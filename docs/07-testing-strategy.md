# Testing Strategy

## Pyramid

```
        ┌───────────────┐
        │   E2E (few)   │   Playwright — critical user journeys only
        ├───────────────┤
        │ Integration   │   Testcontainers (real Postgres/Redis) — API + DB + cache
        ├───────────────┤
        │  Unit (many)  │   JUnit 5 + Mockito — services, rule engine, stats engine
        └───────────────┘
```

## Backend

- **Unit tests** (JUnit 5 + Mockito): flag rule evaluation logic, percentage-rollout consistent hashing, statistical significance calculator (`ExperimentStatsServiceTest` — verify p-value calculation against known reference values), RBAC permission checks.
- **Integration tests** (Testcontainers spinning up real Postgres + Redis, `@SpringBootTest`): repository queries, cache invalidation on flag update, full evaluation flow hitting the actual DB.
- **API/contract tests** (`MockMvc` / `RestAssured`): every controller — happy path + validation-error path + auth-failure path for each endpoint.
- **Coverage target:** 80%+ line coverage enforced via JaCoCo, wired into CI (`mvn verify` fails the build below threshold).

```java
@Test
void percentageRollout_isConsistentForSameUser() {
    // Given a 50% rollout rule, the same userKey must always land in the same bucket
    RolloutRule rule = new RolloutRule("50pct-test", 50);
    boolean firstResult = evaluator.isInRollout(rule, "user-123");
    boolean secondResult = evaluator.isInRollout(rule, "user-123");
    assertThat(firstResult).isEqualTo(secondResult);
}
```

## Frontend

- **Unit tests** (Vitest + React Testing Library): components in isolation (flag toggle, rollout slider, experiment results chart) with mocked API responses.
- **E2E tests** (Playwright): the three journeys that matter most —
  1. Sign up → create project → create flag → toggle it on
  2. Create experiment → simulate exposure/conversion events → view significance result
  3. RBAC: a Viewer-role user cannot see the "Delete Flag" button

## CI Enforcement

Every PR triggers: backend unit + integration tests, JaCoCo coverage gate, frontend unit tests, ESLint/Prettier check, and a Docker build smoke test. See [08-devops-deployment.md](08-devops-deployment.md) for the full pipeline.
