# Project Structure & Git Workflow

## Folder Structure

```
flagforge/
├── backend/
│   ├── src/main/java/com/flagforge/
│   │   ├── config/          # Security, Redis, OpenAPI, CORS config
│   │   ├── controller/      # REST controllers (thin — delegate to services)
│   │   ├── dto/             # Request/response DTOs (never expose entities directly)
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── service/         # Business logic (rule engine, stats engine, RBAC checks)
│   │   ├── security/        # JWT filter, OAuth2 success handler
│   │   └── exception/       # Custom exceptions + @ControllerAdvice
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/    # Flyway SQL migrations
│   ├── src/test/java/...    # Mirrors main structure
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── pages/           # Route-level pages
│   │   ├── api/             # React Query hooks + API client
│   │   ├── hooks/           # Custom hooks (useAuth, useFlagStream)
│   │   ├── context/         # Auth context, theme (dark mode) context
│   │   └── types/           # Shared TypeScript types
│   ├── Dockerfile
│   └── package.json
├── docs/                    # This documentation set
├── .github/workflows/ci.yml
├── docker-compose.yml
├── .env.example
└── README.md
```

## Git Branching Strategy

**Trunk-based with short-lived feature branches:**

```
main            ← always deployable, protected, requires PR + passing CI
  └── develop   ← integration branch, auto-deploys to staging
        └── feature/flag-evaluation-engine
        └── feature/experiment-stats
        └── fix/sse-reconnect-bug
```

- Feature branches: `feature/<short-description>`, `fix/<short-description>`, `chore/<short-description>`
- Branches live < 3 days ideally — small, reviewable PRs over large ones
- `main` is protected: no direct pushes, requires 1 approval + green CI

## Commit Message Convention (Conventional Commits)

```
feat(flags): add percentage-rollout consistent hashing
fix(auth): correct refresh token expiry check
docs(readme): add deployment instructions
test(experiment): add significance calculator unit tests
refactor(cache): extract Redis key builder into utility
chore(deps): bump spring-boot to 3.3.1
```

Format: `<type>(<scope>): <description>` — enables auto-generated changelogs later.

## Pull Request / Code Review Process

1. Open PR against `develop` with a description template: **What changed / Why / How to test / Screenshots (if UI)**.
2. CI must pass (lint, tests, coverage gate, build).
3. Self-review checklist before requesting review: no debug `console.log`/`System.out.println`, no commented-out code, tests added for new logic.
4. At least one review pass required — even solo, use this to simulate real review discipline by reviewing your own diff a day later with fresh eyes.
5. Squash-merge to keep `develop` history clean; the squashed commit message follows the Conventional Commits format above.
