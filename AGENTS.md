# AGENTS.md

This repository is `opentelemetry-java`, the core Java API and SDK implementation for OpenTelemetry.

## Scope and Layout

- This is a large multi-module Gradle build using Kotlin DSL.
- Key top-level modules include:
  - `api`, `context`, `common`
  - `sdk`, `sdk-extensions`
  - `exporters`, `extensions`
  - `integration-tests`
  - `opencensus-shim`, `opentracing-shim`
  - `testing-internal`, plus several `*:testing-internal` helper modules
- Module inclusion is declared in `settings.gradle.kts`. Check there before creating new modules or assuming project paths.

## Environment

- Use Java 21+ for building this repository.
- Published artifacts generally target Java 8+ compatibility unless a module explicitly documents otherwise.
- On Windows, use `gradlew.bat` instead of `./gradlew`.
- Some tests require a local Docker daemon. Those tests are disabled when Docker is unavailable.

## Build and Verification

- Preferred baseline validation:
  - `gradlew.bat check`
- Full build:
  - `gradlew.bat build`
- Auto-format Java and other formatted sources:
  - `gradlew.bat spotlessApply`
- Verify formatting only:
  - `gradlew.bat spotlessCheck`
- If you touch a specific module, prefer targeted Gradle tasks before running the full build.
- Be aware that `check` may regenerate files under `docs/apidiffs/current_vs_latest`; include those changes if they are produced by your change.

## Coding Conventions

- Follow Google Java Style. Formatting is enforced by Spotless.
- Preserve Java 8 source and ABI compatibility for published artifacts unless the module is explicitly incubating/internal and the change is intentional.
- Avoid breaking public APIs. This project follows semver and maintainers expect compatibility across minor and patch releases.
- Default to non-null. Any nullable argument/member should be annotated with `@Nullable`.
- Prefer `final` for public classes when extension is not intended.
- Keep public/protected Javadoc complete and valid.
- Public API additions should normally include `@since` with the next appropriate minor version.
- Avoid synchronizing on `this` or class intrinsic locks; prefer a dedicated lock object.

## Testing and Test Utilities

- Add or update tests with code changes unless the change is truly docs-only or build-only.
- Do not introduce Gradle `java-test-fixtures` for internal test sharing.
- For reusable internal test helpers, follow the repository pattern of dedicated `*:testing-internal` modules.
- Keep tests targeted to the affected module where possible.

## Documentation and Specs

- If behavior changes or new features are added, verify alignment with the OpenTelemetry specification.
- Changes to SDK autoconfigure or configuration options should also be reflected in the relevant docs on `opentelemetry.io`.
- When editing documentation, keep changes minimal and consistent with surrounding style.

## Build Logic and Versioning

- Shared build logic lives in `buildSrc` and convention plugins are used heavily; inspect existing module build files before adding new configuration.
- Repository versions are derived from git tags, not hardcoded in a single version file.
- If version-derived behavior looks stale locally, fetching tags may be required: `git fetch --all --tags`.
- There is a root `generateBuildSubstitutions` task for composite-build substitution snippets.

## Practical Guidance for Agents

- Read the target module's `build.gradle.kts` and nearby package structure before editing.
- Match existing package naming, visibility, and module boundaries; avoid moving classes across published modules without strong justification.
- Prefer small, localized changes over broad refactors.
- When changing public API, check for ripple effects across `all`, BOM, incubator, exporters, shims, and integration tests.
- If a task may touch generated docs or API diff outputs, mention that explicitly in your summary.
