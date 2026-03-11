# AGENTS.md

This repository is `opentelemetry-java`, the core Java API and SDK implementation for OpenTelemetry.

Primary contributor guidance lives in [CONTRIBUTING.md](CONTRIBUTING.md). Read that first and treat
it as the source of truth for repository layout, build and test commands, style expectations, and
scope.

Additional guidance for agents:

* Prefer small, localized changes over broad refactors.
* Read the target module's `build.gradle.kts` and nearby package structure before editing.
* Match existing package naming, visibility, and module boundaries.
* When changing public API, check for ripple effects across `all`, BOM, incubator, exporters,
  shims, and integration tests.
* Mention generated outputs such as `docs/apidiffs/current_vs_latest` when they are affected.
