# API Specification

Full machine-readable spec is generated at runtime via **springdoc-openapi** and served at `/v3/api-docs` (JSON) and `/swagger-ui.html` (interactive). Below is the human-readable summary of core endpoints.

Base URL: `/api/v1`

## Authentication

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/signup` | Create account + organization | Public |
| POST | `/auth/login` | Email/password login → JWT pair | Public |
| POST | `/auth/refresh` | Exchange refresh token for new access token | Refresh token |
| GET | `/oauth2/authorization/google` | Kick off Google OAuth flow | Public |
| POST | `/auth/logout` | Revoke refresh token | Bearer |

**Example — Login**

Request:
```json
POST /api/v1/auth/login
{
  "email": "samriddhi@example.com",
  "password": "correct-horse-battery-staple"
}
```

Response `200 OK`:
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "8f1c2e...",
  "expiresIn": 900,
  "user": { "id": "uuid", "email": "samriddhi@example.com" }
}
```

Response `401 Unauthorized`:
```json
{ "error": "INVALID_CREDENTIALS", "message": "Email or password is incorrect." }
```

## Flags

| Method | Endpoint | Description | Auth | Role |
|---|---|---|---|---|
| GET | `/projects/{projectId}/environments/{envId}/flags` | List flags (paginated, filterable) | Bearer | Viewer+ |
| POST | `/projects/{projectId}/environments/{envId}/flags` | Create flag | Bearer | Editor+ |
| GET | `/flags/{flagId}` | Get flag detail + rules | Bearer | Viewer+ |
| PATCH | `/flags/{flagId}` | Update flag (enable/disable, rules) | Bearer | Editor+ |
| DELETE | `/flags/{flagId}` | Archive flag | Bearer | Admin+ |
| GET | `/evaluate` | **SDK endpoint** — evaluate flags for a user context | API Key | N/A |
| GET | `/stream` | **SDK endpoint** — SSE stream of flag changes | API Key | N/A |

**Example — List flags (paginated + filtered)**
```
GET /api/v1/projects/{projectId}/environments/{envId}/flags?page=0&size=20&search=checkout&enabled=true
```
```json
{
  "content": [
    { "id": "uuid", "key": "new-checkout-flow", "enabled": true, "type": "PERCENTAGE", "updatedAt": "2026-06-01T10:00:00Z" }
  ],
  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1
}
```

**Example — Evaluate (SDK call)**
```
GET /api/v1/evaluate?flagKey=new-checkout-flow
Header: X-API-Key: ffk_client_a1b2c3...
Body: { "userContext": { "userId": "u_123", "country": "IN", "plan": "pro" } }
```
```json
{ "flagKey": "new-checkout-flow", "enabled": true, "variant": "treatment", "reason": "RULE_MATCH" }
```

**Validation rules:**
- `key` — required, `^[a-z0-9-]{3,150}$`, unique per environment
- `rolloutPercentage` — 0–100 inclusive
- `type` — one of `BOOLEAN | MULTIVARIATE | PERCENTAGE`

**Error codes:**
| Code | HTTP Status | Meaning |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Request body failed validation |
| `INVALID_CREDENTIALS` | 401 | Bad login |
| `UNAUTHORIZED` | 401 | Missing/expired JWT or API key |
| `FORBIDDEN` | 403 | Authenticated but role insufficient |
| `FLAG_NOT_FOUND` | 404 | Flag ID/key doesn't exist in scope |
| `DUPLICATE_KEY` | 409 | Flag key already exists in environment |
| `RATE_LIMITED` | 429 | Too many requests (see headers `X-RateLimit-*`) |
| `INTERNAL_ERROR` | 500 | Unhandled server error (logged with correlation ID) |

## Experiments

| Method | Endpoint | Description |
|---|---|---|
| POST | `/flags/{flagId}/experiments` | Create experiment + variants |
| POST | `/experiments/{id}/start` | Move DRAFT → RUNNING |
| POST | `/experiments/{id}/stop` | Move RUNNING → COMPLETED |
| GET | `/experiments/{id}/results` | Exposure/conversion counts + significance |
| POST | `/experiments/{id}/exposure` | **SDK** — record exposure event |
| POST | `/experiments/{id}/conversion` | **SDK** — record conversion event |

**Example — Results**
```json
{
  "experimentId": "uuid",
  "status": "RUNNING",
  "variants": [
    { "name": "control", "exposures": 5000, "conversions": 620, "rate": 0.1240 },
    { "name": "treatment", "exposures": 5000, "conversions": 705, "rate": 0.1410 }
  ],
  "liftPercent": 13.7,
  "pValue": 0.031,
  "significant": true,
  "confidenceLevel": 0.95
}
```

## Audit Log

| Method | Endpoint | Description |
|---|---|---|
| GET | `/projects/{projectId}/audit-log?flagId=&actorId=&from=&to=&page=` | Paginated, filterable audit trail |

## Pagination & Filtering Convention

All list endpoints accept `page` (0-indexed), `size` (default 20, max 100), and resource-specific filters as query params. Responses follow the `{content, page, size, totalElements, totalPages}` envelope consistently.

## Rate Limiting

- `/evaluate` and `/stream`: 1000 req/min per API key (token bucket, Redis-backed)
- Dashboard endpoints: 100 req/min per user
- Exceeding the limit returns `429` with `Retry-After` header
