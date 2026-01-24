## Quick orientation for AI code agents

This repository is a small Quarkus-based Java demo (monolith) that mixes Panache active-record and repository patterns, JAX-RS resources, and an OpenAPI-generated client surface. Use these notes to make edits that match project conventions and to avoid common pitfalls.

- Big picture
  - Monolith service implemented with Quarkus under `src/main/java/com/fulfilment/application/monolith`.
  - Major folders: `products/` (uses a Panache Repository `ProductRepository`), `stores/` (uses PanacheEntity active-record `Store`), `warehouses/` (OpenAPI-generated bindings under `generated-sources/jaxrs` and `src/main/resources/openapi/warehouse-openapi.yaml`).
  - Persistence uses Hibernate ORM with Panache. Entities: `Product` (entity + repository) and `Store` (PanacheEntity).
  - There are simulated integration points to legacy systems: see `LegacyStoreManagerGateway` which is invoked after transaction commit via `TransactionSynchronizationRegistry` in `StoreResource`.

- Project-specific conventions and patterns
  - Mixed Panache styles: repository pattern (e.g. `ProductRepository`) and active-record (e.g. `Store extends PanacheEntity`). When adding new models, follow the existing style used by the package you're changing.
  - JAX-RS resources live next to domain code, annotated with `@Path`, `@Produces`, `@Consumes` and nested `ErrorMapper` provider classes for consistent JSON error responses (see `ProductResource` / `StoreResource`).
  - Transaction-bound side effects are scheduled with `txRegistry.registerInterposedSynchronization(...)` to run only after commit — preserve this approach when adding cross-system notifications.
  - Generated code (OpenAPI/JAX-RS) is under `target/generated-sources/jaxrs` and `generated-sources/jaxrs` in the build; do not edit generated files — update `src/main/resources/openapi/warehouse-openapi.yaml` if you need API changes.

- Build / run / test workflows (concrete commands)
  - Build: run the included Maven wrapper from the repo root:

    ```powershell
    ./mvnw package
    ```

  - Dev mode (hot reload):

    ```powershell
    ./mvnw quarkus:dev
    ```

  - Run tests:

    ```powershell
    ./mvnw test
    ```

  - PostgreSQL for prod profile (example from README):

    ```powershell
    docker run -it --rm --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 15432:5432 postgres:13.3
    ```

  - Notes on config: `src/main/resources/application.properties` defines both dev (H2 in-memory) and `%prod` (Postgres) profiles. By default, dev/test properties use H2; to test PostgreSQL locally use the Docker command above and run the prod profile.

- Integration points & external dependencies to be aware of
  - Database: PostgreSQL in %prod; H2 in dev/test. Files: `src/main/resources/application.properties`, `import.sql` for initial data.
  - OpenAPI generator: `quarkus.openapi.generator.spec` points to `src/main/resources/openapi/warehouse-openapi.yaml` and produces `com.warehouse.api` under generated sources.
  - Legacy gateway: `LegacyStoreManagerGateway` writes a temp file to emulate a legacy call — treat it as the integration boundary when adding async/sync behavior.

- Editing and testing tips
  - Keep JPA entities in `src/main/java/*/domain|products|stores|warehouses` and maintain existing annotations: `@Entity`, `@Cacheable`, Panache usage.
  - Error handling: resources use a nested `ErrorMapper` that converts exceptions to JSON; follow the same pattern for new resources to keep error responses consistent.
  - When changing API surface that originates from OpenAPI, update the YAML spec (`src/main/resources/openapi/warehouse-openapi.yaml`) and regenerate rather than editing generated sources.

If anything above is unclear or you want examples for a particular change (adding an entity, wiring a new post-commit side-effect, or updating the OpenAPI spec), tell me which area and I will expand with targeted examples and edits.
