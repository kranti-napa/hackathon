# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Short answer: Yes — I would standardize the data-access patterns (prefer a repository/service pattern over mixed active-record usage) while keeping small, pragmatic exceptions where they buy clear value.

Reasoning:
- Consistency: Mixing Panache active-record (entities with behavior) and repository-style data access increases cognitive load for maintainers. A single dominant pattern makes it easier to reason about transactions, lifecycle, and testing.
- Testability & Separation of Concerns: Repository classes (or DAOs) give a clear seam for unit testing and mocking. Active-record patterns often couple domain objects to persistence behavior which can make isolated unit tests harder and sometimes encourage business logic leakage into entities.
- Transaction & Lifecycle Control: Repositories make transaction boundaries explicit in service layers. That reduces accidental behavior where persistence-related callbacks or lazy-loading trigger inside web/resource layers.
- Complex Queries & Migrations: For advanced queries, repositories (or a dedicated query layer) are easier to extend with custom SQL/JPA logic, native queries, or query builders. It also simplifies migration to a different persistence strategy if needed.

What I'd do in practice (low-risk incremental plan):
1. Keep the existing code working — do not change public behaviour or generated OpenAPI-backed code.
2. Pick the repository pattern as the preferred approach for new code and for packages where we need fine-grained control (e.g., warehouses/products).
3. Gradually refactor active-record usage into repositories when touching those modules for feature work or bugfixes. Replace domain-level persistence calls with service + repository calls.
4. Add integration tests (Quarkus + H2) around refactored modules to ensure behaviour stays identical.
5. Document the chosen convention in the repo (CONTRIBUTING.md / arch docs) so new contributors follow it.

Exceptions: For tiny, self-contained entities where active-record gives a clear succinct benefit and testability is not impacted, keeping active-record is acceptable. The goal is pragmatic consistency, not dogma.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Summary recommendation: Prefer a contract-first (OpenAPI-first) approach for externally consumed APIs and public services, and use code-first for purely internal or rapidly evolving endpoints — but keep the OpenAPI spec as an authoritative source when possible.

Pros of OpenAPI-first (generate server stubs):
- API contract is explicit, language-agnostic, and becomes a single source of truth for clients.
- Generates documentation, client SDKs, and test vectors automatically, which reduces client/server drift.
- Encourages API design discipline up-front and simplifies backward compatibility decisions.

Cons of OpenAPI-first:
- Generated code can be rigid or verbose; developers may need to learn the generator's conventions.
- Small or internal endpoints that change frequently might feel slowed down by spec-maintenance overhead.

Pros of code-first:
- Faster for small internal features or prototypes; developers can iterate quickly without updating the spec.
- Less ceremony for internal-only endpoints where contract stability isn't required.

Cons of code-first:
- Risk of undocumented/undisciplined API surface; client/server drift if spec or documentation is not kept in sync.

Choice / practical approach:
1. Use OpenAPI-first for any endpoint that is public, consumed by other teams, or is stable enough to merit client SDKs (e.g., Warehouse here looks like a good candidate).
2. For `Product` and `Store`, if they are internal, continue code-first but add a generated OpenAPI spec from the code (or hand-write a spec) before they become public. This gives the best of both worlds.
3. Standardize a workflow: maintain the canonical OpenAPI YAML in `src/main/resources/openapi` for APIs we intend to expose; use generator only for DTOs and interfaces; implement business logic in hand-written classes.

This hybrid approach preserves agility for internal work while ensuring contract-first benefits for external surfaces.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Prioritization summary:
1) Fast unit tests for business logic and utilities (largest volume).
2) Lightweight integration tests for persistence and resource wiring (Quarkus + H2 / Panache) around critical flows.
3) Contract tests for external integrations and for any public API surface.
4) A small set of end-to-end smoke tests for critical user journeys; run these less frequently or in a gated environment.

Concrete actions and rationale:
- Unit tests (highest priority): Fast, deterministic, and run on every push. Cover domain logic, validation, and service-layer decisions. Use builders/test-data factories and mock external dependencies.
- Integration tests (medium priority): Use `@QuarkusTest` with H2 for Panache-backed repositories and resource wiring tests. These catch mapping, transaction, and JPA lifecycle issues that unit tests cannot. Keep the suite small and focused on important flows (create/read/update/delete happy paths, plus a couple of error/edge cases).
- Contract & component tests: For any downstream system (legacy gateway, external APIs), create lightweight contract tests or use consumer-driven-contract tools so we can detect breaking changes early.
- End-to-end and smoke tests (lower priority): Automate a few end-to-end flows that exercise the full stack; execute them in pre-prod or nightly CI because they are slower and more brittle.

Keeping coverage effective over time:
- Track meaningful coverage metrics (branch and line coverage) but avoid blindly chasing percentage. Prefer tests that verify behavior and failure modes, not just lines executed.
- Monitor flaky tests and quarantine or fix them immediately — flakiness erodes trust and slows development.
- Use CI gating: run unit tests on every push, integration tests on pull requests or a matrix, end-to-end on nightly or release pipelines.
- Make tests fast and parallel where possible. Use in-memory databases (H2) for most persistence tests and Testcontainers only when you need a production-like environment.
- Add a small suite of targeted mutation tests or critical-path assertions to ensure tests meaningfully protect behavior.
- Keep tests maintainable: use builders, fixtures, and helper assertions; avoid duplicating setup; prefer explicit assertions over large opaque fixtures.

Operational suggestions:
- Add CI thresholds that fail the build for regressions in critical tests but avoid hard-blocking on total coverage percentage; instead enforce minimums per-package for business-critical modules.
- Add a lightweight health-check and smoke job that runs fast and alerts on infra issues before more expensive jobs run.

This strategy balances speed, reliability, and confidence while allowing the team to increase coverage pragmatically where it matters most.
```