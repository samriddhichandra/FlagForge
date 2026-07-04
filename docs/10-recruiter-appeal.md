# Recruiter Appeal & Interview Prep

## Skills This Project Demonstrates

- **Backend engineering:** Spring Boot, layered architecture (controller/service/repository), clean separation via DTOs, Bean Validation, global exception handling.
- **Distributed-systems fundamentals:** caching strategy with explicit invalidation, pub/sub for real-time fan-out, consistent hashing for stable percentage rollouts, rate limiting.
- **Database design:** normalized relational schema with deliberate denormalization (JSONB) where flexibility matters, indexing tied to actual query patterns, query optimization backed by `EXPLAIN ANALYZE`.
- **Security:** JWT + OAuth2, RBAC, OWASP Top 10 mitigations, secrets management.
- **Applied statistics:** two-proportion significance testing — a differentiator most student projects don't attempt.
- **Full-stack delivery:** React + TypeScript SPA consuming a real API, real-time UI updates via SSE.
- **DevOps:** Docker, Docker Compose, CI/CD via GitHub Actions, health checks, free-tier cloud deployment.
- **Testing discipline:** unit/integration/E2E pyramid, enforced coverage gate in CI.
- **Engineering judgment:** explicit, written trade-off reasoning (modular monolith vs. microservices, Redis Streams vs. Kafka) — this is what separates "did a tutorial" from "can reason about a system," and it's the single most interview-relevant artifact in the whole project.

## Likely Interview Questions This Project Sets You Up For

**System design:**
- "How would you design a feature flag system?" → you *built* one; walk through the real architecture diagram.
- "How do you keep a cache consistent with the source of truth?" → explain your write-invalidate + TTL-safety-net approach.
- "How would this scale to 1M users?" → walk through your scalability doc's per-tier bottleneck analysis.

**Backend/Java:**
- "Explain the difference between `@Transactional` propagation types" (relevant if you added multi-step flag+audit-log writes).
- "How do you prevent a cache stampede?" → `SETNX`-based locking, explained in your scalability doc.
- "Walk me through your RBAC implementation — why check the DB and not just trust the JWT claims?"

**Database:**
- "Why did you choose JSONB for the rule condition instead of a rigid schema?" → flexibility vs. queryability trade-off.
- "How would you handle a table that's growing too large?" → partitioning strategy for event tables.

**Statistics/product sense:**
- "How do you know when an A/B test result is statistically significant, not just noise?" → walk through your p-value/confidence-interval implementation. This question alone often separates junior from mid-level candidates — very few new grads can answer it concretely, and you'll have code to point to.

**Behavioral:**
- "Tell me about a technical trade-off you made." → modular monolith vs. microservices, or Redis Streams vs. Kafka — both are documented with explicit reasoning in this repo, ready to discuss.

## How to Present This on a Resume

> **FlagForge** — Multi-tenant feature flag & A/B testing platform (Spring Boot, React, PostgreSQL, Redis)
> - Designed and built a real-time flag evaluation engine (SSE + Redis pub/sub) serving sub-second config propagation to connected clients
> - Implemented a statistical significance engine (two-proportion z-test) for A/B experiment analysis
> - Enforced RBAC across a multi-tenant data model (org → project → environment) with a full audit trail
> - Achieved 80%+ backend test coverage (JUnit, Testcontainers) with CI/CD via GitHub Actions and Docker

Keep it to 3–4 bullets on the resume itself — the depth lives in this repo for when an interviewer asks "tell me more."
