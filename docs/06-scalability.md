# Scalability

## Scaling Narrative: 10 → 1M Users

| Scale | Bottleneck | Mitigation |
|---|---|---|
| **10 users** | None — single Docker Compose stack (1 app instance, 1 Postgres, 1 Redis) handles this trivially. | N/A — focus on correctness, not scale. |
| **1,000 users** | Flag evaluation calls become frequent enough that hitting Postgres on every request is wasteful. | Redis cache in front of flag rules (already in base design) drops DB load by ~95%+ for read-heavy evaluate traffic. Add DB connection pooling tuning (HikariCP, `maximumPoolSize` sized to `(core_count * 2) + effective_spindle_count`). |
| **100,000 users** | (a) Single app instance becomes a CPU/memory bottleneck. (b) SSE connections held open per environment start to strain a single instance's file descriptor limits. (c) Experiment event writes (exposure/conversion) become high-volume. | (a) Horizontally scale the Spring Boot app behind a load balancer — the app is already stateless (JWT auth, no in-memory session), so this is a config change, not a redesign. (b) SSE connections are backed by Redis pub/sub, so *any* instance can serve *any* environment's stream — sticky sessions aren't required. (c) Batch event writes: SDKs buffer exposure/conversion events client-side and flush every N seconds or M events, cutting write QPS by an order of magnitude. |
| **1,000,000 users** | (a) Postgres primary becomes a write bottleneck for high-volume event tables. (b) Cross-region latency for globally distributed SDK callers. (c) Cache stampede risk if a popular flag's cache entry expires under heavy concurrent load. | (a) Partition `exposure_event`/`conversion_event` by month (Postgres native partitioning) and consider a read replica for the analytics/results queries so they don't compete with the write path. At true 1M-DAU volume, migrating event ingestion to Kafka + a stream processor (Flink/ksqlDB) for pre-aggregation becomes worth the operational cost — called out here as the next evolution, not built, since it's outside a 6–10 week single-dev scope. (b) Deploy read-through evaluation nodes (a lightweight "relay proxy," same idea as LaunchDarkly's Relay Proxy) in each region, each with its own Redis cache, polling the origin for updates rather than every SDK call crossing the ocean. (c) Use Redis's `SETNX`-based lock ("cache stampede protection") so only one request repopulates a hot cache key while others wait briefly, instead of all of them hammering Postgres simultaneously. |

## Concrete Numbers to Capture (fill in after load testing)

Run `k6` or `Gatling` against the `/evaluate` endpoint and document in the README:
- p50/p95/p99 latency at 100, 1,000, 10,000 concurrent virtual users
- Cache hit ratio under sustained load
- Max sustained QPS on a single free-tier instance before error rate climbs

> Recruiters and interviewers respond far better to "I load tested this and got p95 of 40ms at 5k req/s on a $0 Render instance" than to unverified scalability claims — treat this section as a checklist to actually execute, not just write about.

## Database Scaling Levers (in order of when you'd reach for them)

1. Add indexes matching real query patterns (already done in schema design).
2. Connection pooling tuned to instance size.
3. Read replica for reporting/analytics queries.
4. Table partitioning for time-series event tables.
5. Caching layer (Redis) in front of hot reads — already in place from day one.
6. Sharding — genuinely only relevant past the 1M+ user mark for this workload; explicitly out of scope, and calling that out correctly is itself a signal of engineering maturity (knowing when *not* to over-engineer).
