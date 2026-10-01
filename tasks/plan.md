# Implementation Plan: Complete Phase 0 Foundation

## Scope

Complete the remaining Phase 0 foundation without changing Spring Security:
OpenAPI metadata, validated application configuration, Actuator health/readiness,
governance persistence models, and PostgreSQL integration coverage.

## Ordered slices

1. Add OpenAPI and Actuator dependencies/configuration, with endpoint tests.
2. Add validated `aether.*` configuration properties and startup validation tests.
3. Add ProviderCredential, RoutingPolicy, UsageRecord, Budget, and AuditEvent entities,
   repositories, enums, and Flyway migration.
4. Add a PostgreSQL Testcontainers repository integration test and an opt-in Gradle task.
5. Run focused tests and the full test suite; review schema/API compatibility.

## Decisions

- Governance entities are persistence contracts in Phase 0; public management APIs are
  deferred until routing, usage, and authorization semantics are defined.
- Credentials store a secret reference/hash, never plaintext provider secrets.
- PostgreSQL integration tests are opt-in when Docker is unavailable locally, but remain
  executable through a dedicated Gradle task in CI.
