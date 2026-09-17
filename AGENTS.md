# AGENTS.md

## Build & Run

- `./gradlew bootRun` — starts the app (loads `.env` for DB credentials)
- `./gradlew build` — compile + test; jOOQ codegen runs automatically before compilation
- `./gradlew jooqCodegen` — regenerate jOOQ classes from MySQL schema manually
- `./gradlew test` — runs JUnit 5 tests

## Prerequisites

- **MySQL must be running** on `localhost:3306` with schema `ktjooq` for both `bootRun` and `jooqCodegen`
- `.env` file is required at project root (gitignored). Copy from `.env` if missing; contains DB credentials
- Java 21 toolchain is used; `JAVA_HOME` must point to JDK 21+

## Code Generation (jOOQ)

- Generated sources land in `build/generated-src/jooq` under package `com.vaddshah.ktjooq.generated`
- These are compiled as part of main sources (configured in `sourceSets`)
- KotlinCompile tasks depend on `jooqCodegen` — you do NOT need to run codegen separately before building
- If you modify the DB schema, run `./gradlew jooqCodegen` then rebuild. Generated code is gitignored
- Codegen excludes `flyway_schema_history` table and only targets the `ktjooq` schema

## Migrations

- Flyway SQL files live in `src/main/resources/db/migration/`
- Versioned with `V<version>__<description>.sql` naming
- Migrations run automatically on app startup
- Adding a migration: create a new file with the next version number (e.g. `V2__add_orders_table.sql`)

## Architecture

- **Single-module** Kotlin/Spring Boot app (not a monorepo)
- Feature-based packages: `src/main/kotlin/com/vaddshah/ktjooq/features/<feature>/`
- Each feature has: `Controller` → `Service` → `Repository` (jOOQ `DSLContext`)
- DTOs live in `features/<feature>/dtos/`
- Common API types in `common/api/`: `ErrorResponse`, `PageResponse`, `GlobalExceptionHandler`
- jOOQ generated tables/records: `com.vaddshah.ktjooq.generated.tables.*`
- REST API base path: `/api/<feature>` (e.g. `/api/users`)

## Conventions

- Use `@field:` prefix for validation annotations on Kotlin data class constructor parameters
- Repository layer maps jOOQ records to DTOs manually (no AutoMapper)
- Error responses use `ErrorResponse` data class with status, message, timestamp, and optional field errors
- `.env` is loaded by `springboot4-dotenv` at runtime and by `loadDotEnv()` in `build.gradle.kts` for codegen
- The `.env` file is gitignored — never commit real credentials

## Testing

- Test class: `src/test/kotlin/com/vaddshah/ktjooq/KtjooqApplicationTests.kt`
- Uses `@SpringBootTest` which loads full application context — requires MySQL connection
- No testcontainers or embedded DB configured; integration tests hit real MySQL
- To run a single test: `./gradlew test --tests "com.vaddshah.ktjooq.KtjooqApplicationTests.contextLoads"`

## Gotchas

- If `jooqCodegen` fails, check that MySQL is running and `.env` credentials are correct
- Generated jOOQ code is not checked into git — a build without prior codegen will fail to compile
- `spring.jooq.sql-dialect=mysql` must be set in `.env` for jOOQ SQL generation
