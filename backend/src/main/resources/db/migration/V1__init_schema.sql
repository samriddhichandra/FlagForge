-- Baseline schema migration. Full DDL with rationale lives in docs/03-database-design.md.
-- This file is the source of truth Flyway actually applies; keep it in sync with the docs.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    oauth_provider VARCHAR(50),
    oauth_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_auth_method CHECK (password_hash IS NOT NULL OR oauth_provider IS NOT NULL)
);

CREATE TABLE membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER','ADMIN','EDITOR','VIEWER')),
    UNIQUE (user_id, organization_id)
);

CREATE TABLE project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organization_id, slug)
);

CREATE TABLE environment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL,
    UNIQUE (project_id, slug)
);

CREATE TABLE api_key (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    environment_id UUID NOT NULL REFERENCES environment(id) ON DELETE CASCADE,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    key_prefix VARCHAR(12) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('SERVER','CLIENT')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ
);
CREATE INDEX idx_api_key_prefix ON api_key (key_prefix);

CREATE TABLE flag (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    environment_id UUID NOT NULL REFERENCES environment(id) ON DELETE CASCADE,
    key VARCHAR(150) NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('BOOLEAN','MULTIVARIATE','PERCENTAGE')),
    enabled BOOLEAN NOT NULL DEFAULT false,
    default_value JSONB NOT NULL DEFAULT 'false',
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (environment_id, key)
);
CREATE INDEX idx_flag_environment ON flag (environment_id);

CREATE TABLE flag_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_id UUID NOT NULL REFERENCES flag(id) ON DELETE CASCADE,
    priority INT NOT NULL DEFAULT 0,
    condition JSONB NOT NULL,
    value JSONB NOT NULL,
    rollout_percentage INT CHECK (rollout_percentage BETWEEN 0 AND 100)
);
CREATE INDEX idx_flag_rule_flag ON flag_rule (flag_id, priority);

CREATE TABLE experiment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_id UUID NOT NULL REFERENCES flag(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    goal_metric VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','RUNNING','COMPLETED')),
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ
);

CREATE TABLE experiment_variant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    experiment_id UUID NOT NULL REFERENCES experiment(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    allocation_percentage INT NOT NULL CHECK (allocation_percentage BETWEEN 0 AND 100),
    is_control BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE exposure_event (
    id BIGSERIAL PRIMARY KEY,
    variant_id UUID NOT NULL REFERENCES experiment_variant(id),
    user_key VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_exposure_variant_time ON exposure_event (variant_id, occurred_at);

CREATE TABLE conversion_event (
    id BIGSERIAL PRIMARY KEY,
    variant_id UUID NOT NULL REFERENCES experiment_variant(id),
    user_key VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_conversion_variant_time ON conversion_event (variant_id, occurred_at);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id UUID REFERENCES app_user(id),
    flag_id UUID REFERENCES flag(id),
    action VARCHAR(50) NOT NULL,
    before_state JSONB,
    after_state JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_flag_time ON audit_log (flag_id, created_at DESC);
CREATE INDEX idx_audit_actor_time ON audit_log (actor_user_id, created_at DESC);
