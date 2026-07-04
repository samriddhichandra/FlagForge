# Product Vision

## Problem Statement

Software teams need to decouple **deploying code** from **releasing features**. Without a flagging system, teams either:
- Ship risky all-or-nothing releases (a bad deploy means a bad rollback under pressure), or
- Hardcode conditionals (`if (user.id == 42)`) that rot into unmaintainable spaghetti, or
- Buy an expensive third-party tool (LaunchDarkly starts around $10–75/seat/month) when budgets are tight.

FlagForge gives engineering teams flag-based rollouts and lightweight A/B testing without vendor lock-in or per-seat pricing.

## Target Users

| Persona | Need |
|---|---|
| **Backend/Full-stack engineer** | Ship code behind a flag, toggle it on/off without a redeploy |
| **Product manager** | Run an A/B test and see which variant wins, without asking an engineer to pull logs |
| **Engineering lead** | Kill-switch a broken feature in production in <5 seconds; audit who changed what |
| **QA/Support** | See which flags are active for a specific user/environment when debugging |

## Market Need

Feature flagging is now considered standard practice at any company doing continuous delivery (progressive delivery, canary releases, trunk-based development all depend on it). Every FAANG company has an internal flagging system; smaller companies either buy LaunchDarkly/Split.io or build ad-hoc, buggy versions in-house. FlagForge demonstrates you understand *why* this category of tool exists — a signal that reads well in an SDE interview regardless of the specific company.

## User Journey

1. **Sign up / log in** (email+password or Google OAuth) → creates an Organization.
2. Create a **Project** (e.g. "Checkout Service") and **Environments** (dev/staging/prod) under it.
3. Create a **Flag** (`new-checkout-flow`) — boolean, percentage rollout, or multivariate.
4. Install the **SDK** in the target app, initialize with an environment-scoped API key.
5. App calls `flagforge.isEnabled("new-checkout-flow", userContext)` — evaluated server-side, cached client-side, updated in real time via SSE.
6. PM converts the flag into an **Experiment**: define control/variant split, target metric (e.g. `checkout_completed` event).
7. SDK reports exposure + conversion events → Experiment Service aggregates → dashboard shows live lift % and statistical significance.
8. Once a winner is clear, PM/engineer "graduates" the flag (rolls out to 100%) and archives the experiment. Every step is logged in the **audit trail**.

## Competitive Advantage (vs. building it yourself, ad-hoc)

- **Real-time propagation** — sub-second flag updates via SSE + Redis pub/sub, not "poll every 60 seconds and hope."
- **Statistical rigor built in** — significance testing is computed server-side, so PMs don't need a data scientist to interpret results.
- **Full audit trail by design** — every mutation is append-only logged; compliance-friendly from day one.
- **Multi-tenant from the ground up** — environment isolation prevents a dev flag flip from leaking into prod.
