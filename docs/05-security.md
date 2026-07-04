# Security

## Authentication

- **Dashboard users:** JWT access tokens (15 min TTL) + rotating refresh tokens (7 day TTL, stored hashed in DB, revocable on logout). Access tokens are stateless (signed with RS256, not HS256, so the public key can be shared with other services later without exposing the signing key).
- **OAuth2:** Google login via Spring Security's OAuth2 client; on first login, an account + organization is auto-provisioned.
- **SDK/API clients:** Long-lived API keys, scoped to a single environment and type (`SERVER` — can evaluate flags with full targeting; `CLIENT` — restricted, safe to embed in a browser bundle since it can only reach public-facing flag state, never mutate).
- **Password storage:** BCrypt, cost factor 12. Never logged, never returned in any API response (enforced via a dedicated DTO that excludes the field, not just `@JsonIgnore` on the entity — entities are never serialized directly).

## Authorization (RBAC)

| Role | Permissions |
|---|---|
| Viewer | Read flags, experiments, audit log |
| Editor | + create/update flags, rules, experiments |
| Admin | + delete/archive flags, manage API keys |
| Owner | + manage organization members, billing (future) |

Enforced via a custom `@PreAuthorize("@rbac.hasRole(#projectId, 'EDITOR')")` method-security expression, checked against the `membership` table — not just baked into the JWT — so a role downgrade takes effect immediately instead of waiting for token expiry.

## OWASP Top 10 Mitigations

| Risk | Mitigation |
|---|---|
| **Injection (SQL)** | Spring Data JPA + parameterized queries exclusively; no string-concatenated JPQL/native queries. `@Query` uses named parameters. |
| **Broken Authentication** | BCrypt hashing, short-lived JWTs, refresh token rotation + revocation list in Redis. |
| **Sensitive Data Exposure** | TLS everywhere (Nginx terminates HTTPS), API keys stored as SHA-256 hashes, secrets via environment variables / Docker secrets, never committed. |
| **XXE** | No XML parsing in the request path; Jackson (JSON) only. |
| **Broken Access Control** | Method-level `@PreAuthorize` on every mutating endpoint; resource ownership double-checked in the service layer (defense in depth — don't trust the URL path alone). |
| **Security Misconfiguration** | `application-prod.yml` disables verbose error stack traces (`server.error.include-stacktrace=never`), Actuator endpoints restricted to `/actuator/health` publicly. |
| **XSS** | React escapes output by default; API responses are `application/json` only (never reflect raw HTML); `Content-Security-Policy` header set via Nginx. |
| **Insecure Deserialization** | Jackson configured with `FAIL_ON_UNKNOWN_PROPERTIES` and no polymorphic type handling from untrusted input. |
| **Vulnerable Components** | `mvn dependency-check` / `npm audit` run in CI; Dependabot enabled on the repo. |
| **Insufficient Logging** | Every auth event and flag mutation is logged with correlation ID; audit log is append-only and immutable at the application layer (no `UPDATE`/`DELETE` endpoint exists for `audit_log` rows). |

## CSRF

Since the API is stateless (JWT in `Authorization` header, not cookies, for the dashboard SPA), classic CSRF (which relies on ambient cookie auth) doesn't apply to the JWT-authenticated endpoints. The one cookie-based flow is the refresh token, stored as an `HttpOnly`, `Secure`, `SameSite=Strict` cookie — `SameSite=Strict` is the primary CSRF defense here, and Spring Security's CSRF filter remains enabled for that cookie-authenticated path.

## Rate Limiting

Redis-backed token bucket, keyed by API key (SDK endpoints) or user ID (dashboard endpoints). Implemented as a Spring `HandlerInterceptor` so it runs before controller logic, not duplicated per-endpoint.

## Secrets Management

- Local dev: `.env` file (git-ignored), loaded via Docker Compose `env_file`.
- Production: environment variables injected by the hosting platform (Render/Railway secrets manager) — never baked into the Docker image.

## Input Validation

- Bean Validation (`jakarta.validation`) annotations on every DTO (`@NotBlank`, `@Pattern`, `@Min`/`@Max`).
- A global `@ControllerAdvice` catches `MethodArgumentNotValidException` and returns a consistent `VALIDATION_ERROR` shape (see API spec) instead of leaking a raw stack trace.
