# System Architecture

## High-Level Architecture

```mermaid
flowchart TB
    subgraph Clients
        WEB["React SPA (Dashboard)"]
        SDK["Server/Client SDKs (in customer apps)"]
    end

    subgraph Edge
        NGINX["Nginx Reverse Proxy / TLS termination"]
    end

    subgraph AppLayer["Spring Boot Application (modular monolith)"]
        AUTH["Auth Module\nJWT + OAuth2"]
        FLAG["Flag Module\nCRUD + Evaluation Engine"]
        EXP["Experiment Module\nStats Engine"]
        AUDIT["Audit Module\nAppend-only Log"]
        SSE["Realtime Module\nSSE Broadcaster"]
    end

    subgraph Data
        PG[("PostgreSQL 16\nPrimary DB")]
        REDIS[("Redis 7\nCache + Pub/Sub")]
    end

    subgraph Ops
        PROM["Prometheus"]
        GRAF["Grafana"]
        LOGS["Structured JSON logs\n(stdout -> log aggregator)"]
    end

    WEB -->|HTTPS/REST| NGINX
    SDK -->|HTTPS/REST + SSE| NGINX
    NGINX --> AUTH
    NGINX --> FLAG
    NGINX --> EXP
    NGINX --> SSE

    AUTH --> PG
    FLAG --> PG
    FLAG --> REDIS
    EXP --> PG
    AUDIT --> PG
    SSE --> REDIS

    FLAG -.->|publishes change event| REDIS
    REDIS -.->|pub/sub notifies| SSE

    AppLayer --> PROM
    PROM --> GRAF
    AppLayer --> LOGS
```

**Why a modular monolith, not microservices?** At the scale this project targets (single dev, 6–10 weeks, portfolio-grade), microservices add network-boundary complexity (service discovery, distributed tracing, inter-service auth) without a corresponding benefit — there's no team-scaling problem to solve yet. The codebase is structured in **clearly bounded modules** (`auth`, `flag`, `experiment`, `audit`) with well-defined interfaces, so it could be split into services later with minimal rework. This is itself a good interview talking point: "I designed for a modular monolith now, with clear seams to extract services later, because Conway's Law says microservices should mirror team boundaries — and there's one team."

## Flag Evaluation & Real-Time Propagation Flow

```mermaid
sequenceDiagram
    participant SDK as Client SDK
    participant API as Flag Service
    participant Cache as Redis
    participant DB as PostgreSQL
    participant Bus as Redis Pub/Sub
    participant Conn as SSE Connection Manager

    SDK->>API: GET /api/v1/flags/evaluate (API key + user context)
    API->>Cache: GET flag:{envId}:{flagKey}
    alt Cache hit
        Cache-->>API: flag rules (JSON)
    else Cache miss
        API->>DB: SELECT flag + rules
        DB-->>API: flag rules
        API->>Cache: SET flag:{envId}:{flagKey} (TTL 60s)
    end
    API->>API: Evaluate rules against user context\n(percentage rollout hash, targeting rules)
    API-->>SDK: { enabled: true, variant: "treatment" }

    Note over API,Bus: When an admin changes a flag in the dashboard:
    API->>DB: UPDATE flag
    API->>Cache: DEL flag:{envId}:{flagKey}
    API->>Bus: PUBLISH channel:env:{envId} { flagKey, newState }
    Bus->>Conn: notify subscribers
    Conn-->>SDK: SSE event: flag-updated
    SDK->>SDK: re-fetch or apply pushed state instantly
```

## Component Responsibilities

| Component | Responsibility |
|---|---|
| **API Gateway (Nginx)** | TLS termination, request routing, rate-limit headers, gzip |
| **Auth Module** | Signup/login, JWT issuance + refresh, OAuth2 (Google), password hashing (BCrypt) |
| **Flag Module** | Flag CRUD, rule engine (percentage rollout via consistent hashing, user targeting), evaluation endpoint |
| **Experiment Module** | Experiment lifecycle, exposure/conversion event ingestion, statistical significance calculation |
| **Audit Module** | Append-only log of every mutating action, queryable by actor/resource/time |
| **Realtime Module** | Manages SSE connections per environment, broadcasts on Redis pub/sub message |
| **Redis** | (1) Flag-rule cache to avoid DB hit on every evaluation call, (2) pub/sub bus for real-time fan-out |
| **PostgreSQL** | System of record: orgs, projects, environments, flags, rules, experiments, events, audit log |

## Notifications & Monitoring

- **Notifications:** Webhook dispatch (outbound HTTP POST) on flag state change — foundation for future Slack/Discord integration. Implemented as an async `@EventListener` + retry queue, not inline in the request path (so a slow webhook receiver never slows down a flag evaluation call).
- **Logging:** Structured JSON logs (via Logback + `logstash-logback-encoder`) to stdout, so they're container-log-driver friendly (Docker → any aggregator).
- **Monitoring:** Spring Boot Actuator exposes `/actuator/prometheus`; Prometheus scrapes it; Grafana dashboards for request latency, flag-evaluation QPS, cache hit ratio, SSE connection count.
- **Analytics:** Experiment exposure/conversion events feed the stats engine — this *is* the product's analytics layer (see Experiment Module).

## Caching Strategy

Flag rules are cached in Redis with a **60-second TTL as a safety net**, but the primary invalidation path is **explicit cache-busting on write**: any flag mutation immediately does `DEL` on the cache key and publishes the change. This gives strong consistency (no waiting for TTL expiry) while the TTL protects against a missed invalidation event turning into permanent staleness.

## Message Queue Usage

Rather than a heavyweight broker (Kafka/RabbitMQ) for a single-instance portfolio deployment, **Redis Streams** is used for the experiment event ingestion pipeline (`XADD` on exposure/conversion events, a consumer group processes them into aggregate stats asynchronously). This is called out explicitly in the docs as a trade-off: "In a real multi-service production system with high event volume, this would be Kafka for durability and replay; Redis Streams was chosen here because it's operationally simpler for a single-node deployment and the throughput ceiling (~10k events/sec) is far above what this system needs at portfolio scale."
