# Build

Java 21 or higher is required to build. The built artifacts target Java 8 or higher.

## Common tasks

```bash
# Fix formatting violations
./gradlew spotlessApply

# Run checks only (tests + static analysis + formatting verification)
./gradlew check

# Build, run tests, static analysis, and check formatting
./gradlew build
```

All tasks can be scoped to a single module by prefixing with the module path:

```bash
./gradlew :sdk:metrics:spotlessApply
./gradlew :sdk:metrics:check
./gradlew :sdk:metrics:build
```

`./gradlew build` and `./gradlew check` both depend on the `jApiCmp` task, which compares the
locally-built jars against the latest release and writes diffs to `docs/apidiffs/current_vs_latest/`.
Include any changes to those files in your PR. See [api-design.md](api-design.md#japicmp)
for details.

If your branch is not up to date with `main`, `jApiCmp` may produce a diff that reflects changes
already merged to `main` rather than your own changes. Rebase or merge `main` before treating
the diff as meaningful.

For dev environment setup, composite builds, OTLP protobuf updates, and other tasks, see
[other-tasks.md](other-tasks.md).
