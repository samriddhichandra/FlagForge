# FlagForge

FlagForge is a multi-tenant feature flag and experimentation platform built with Spring Boot, React, PostgreSQL, and Redis. It demonstrates the core systems behind products like LaunchDarkly and Split: safe rollouts, flag evaluation, role-aware administration, auditability, and experiment analysis.

## Highlights

- Feature flag CRUD with environment-scoped uniqueness.
- Percentage rollout bucketing with deterministic hashing.
- Experiment significance calculations using a two-proportion z-test.
- Redis-backed cache invalidation path for low-latency flag reads.
- React dashboard with optimistic flag toggles and Server-Sent Events wiring.
- PostgreSQL schema managed by Flyway migrations.
- Docker Compose stack for local full-system runs.
- GitHub Actions workflow for backend tests, frontend checks, and Docker build smoke tests.

## Architecture

```text
React dashboard
    |
    | REST / SSE
    v
Spring Boot API  ---- Redis
    |
    | JPA / Flyway
    v
PostgreSQL
```

For deeper design notes, see:

| Document | Purpose |
| --- | --- |
| [Product Vision](docs/01-product-vision.md) | Users, problem statement, product scope |
| [System Architecture](docs/02-system-architecture.md) | Service boundaries and runtime design |
| [Database Design](docs/03-database-design.md) | Tables, indexes, and relationships |
| [API Specification](docs/04-api-specification.md) | Endpoint contracts and examples |
| [Security](docs/05-security.md) | Auth, RBAC, API keys, and OWASP notes |
| [Scalability](docs/06-scalability.md) | Scaling plan from prototype to high traffic |
| [Testing Strategy](docs/07-testing-strategy.md) | Unit, integration, and E2E approach |
| [DevOps and Deployment](docs/08-devops-deployment.md) | CI/CD and hosting plan |
| [Project Structure](docs/09-project-structure.md) | Repository layout |
| [Recruiter Appeal](docs/10-recruiter-appeal.md) | Skills demonstrated and interview prompts |

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16, Flyway |
| Cache | Redis 7 |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, React Query |
| Testing | JUnit 5, AssertJ, Vitest |
| Delivery | Docker, Docker Compose, GitHub Actions, Nginx |

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker and Docker Compose

## Local Development

1. Copy the environment template:

   ```bash
   cp .env.example .env
   ```

2. Start dependencies:

   ```bash
   docker compose up postgres redis
   ```

3. Run the backend:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. Run the frontend in a second terminal:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. Open the app:

   - Frontend: http://localhost:5173
   - Backend health: http://localhost:8080/actuator/health
   - Swagger UI: http://localhost:8080/swagger-ui.html

## Docker Compose

Build and run the full stack:

```bash
docker compose up --build
```

Services:

| Service | Port |
| --- | --- |
| Frontend | 5173 |
| Backend API | 8080 |
| PostgreSQL | 5432 |
| Redis | 6379 |

## Testing and Quality

Backend:

```bash
cd backend
mvn test
mvn verify
```

Frontend:

```bash
cd frontend
npm install
npm run lint
npm run test
npm run build
```

## API Quick Start

Create a flag:

```bash
curl -X POST "http://localhost:8080/api/v1/projects/{projectId}/environments/{environmentId}/flags" \
  -H "Content-Type: application/json" \
  -d '{"key":"new-checkout","name":"New Checkout","type":"BOOLEAN"}'
```

List flags:

```bash
curl "http://localhost:8080/api/v1/projects/{projectId}/environments/{environmentId}/flags?search=checkout"
```

Toggle a flag:

```bash
curl -X PATCH "http://localhost:8080/api/v1/projects/{projectId}/environments/{environmentId}/flags/{flagId}/enabled?enabled=true"
```

## Environment Variables

| Variable | Default | Description |
| --- | --- | --- |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `flagforge` | PostgreSQL database |
| `DB_USER` | `flagforge` | PostgreSQL user |
| `DB_PASSWORD` | `flagforge` | PostgreSQL password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `GOOGLE_CLIENT_ID` | empty | OAuth client id |
| `GOOGLE_CLIENT_SECRET` | empty | OAuth client secret |

## Production Notes

- Commit a package lockfile before relying on deterministic frontend builds.
- Replace placeholder RBAC and API-key scaffolding with persistent membership and key validation before public deployment.
- Use managed secrets instead of `.env` files in hosted environments.
- Keep Flyway migrations as the only source of schema changes; Hibernate is configured with `ddl-auto=validate`.
- Expose only health endpoints publicly unless metrics are protected behind internal networking.

## License

No license has been declared yet. Add one before distributing or accepting external contributions.
