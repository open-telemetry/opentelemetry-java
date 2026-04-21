# Other Tasks

## Benchmarks (JMH)

Microbenchmarks use [JMH](https://github.com/openjdk/jmh) via the `otel.jmh-conventions` plugin.
Benchmark sources live in `jmh/` directories within their respective modules.

```bash
# Run all benchmarks for a module
./gradlew :sdk:trace:jmh

# Run a single benchmark class
./gradlew -PjmhIncludeSingleClass=BatchSpanProcessorBenchmark :sdk:trace:jmh
```

The following JMH parameters can be configured via `-P` flags:

| Flag | JMH setting | Example |
|---|---|---|
| `-PjmhIncludeSingleClass=<class>` | Run only this benchmark class | `-PjmhIncludeSingleClass=BatchSpanProcessorBenchmark` |
| `-PjmhFork=<n>` | Number of forks | `-PjmhFork=1` |
| `-PjmhIterations=<n>` | Measurement iterations | `-PjmhIterations=5` |
| `-PjmhTime=<duration>` | Time per measurement iteration | `-PjmhTime=2s` |
| `-PjmhWarmupIterations=<n>` | Warmup iterations | `-PjmhWarmupIterations=3` |
| `-PjmhWarmup=<duration>` | Time per warmup iteration | `-PjmhWarmup=1s` |

## Composite builds

> **No compatibility guarantees**: this process can change at any time. Steps that work for one
> commit may break with the next.

To test local changes to this repo against another project (e.g. instrumentation or an app),
use Gradle [composite builds](https://docs.gradle.org/current/userguide/composite_builds.html).

This repo does not work with composite builds out of the box because Gradle cannot automatically
map project names to the customized Maven artifact coordinates (see
[gradle/gradle#18291](https://github.com/gradle/gradle/issues/18291)). A helper task generates
the required substitution mappings.

1. Run `./gradlew generateBuildSubstitutions` — generates `build/substitutions.gradle.kts` in
   the `bom/` and `bom-alpha/` directories, containing substitutions for stable and alpha
   projects respectively.
2. In the consuming project's `settings.gradle.kts`, add an `includeBuild` block and paste in
   the generated `dependencySubstitution` content:

   ```kotlin
   includeBuild("PATH/TO/OPENTELEMETRY-JAVA/ROOT/DIRECTORY") {
     // paste from generated substitutions.gradle.kts
     dependencySubstitution {
       substitute(module("io.opentelemetry:opentelemetry-api")).using(project(":api:all"))
       substitute(module("io.opentelemetry:opentelemetry-sdk")).using(project(":sdk:all"))
       // ...
     }
   }
   ```

3. Confirm the local version aligns with the version declared in the consuming project —
   if they differ, `dependencySubstitution` may not take effect.
4. Use the prefix `:DIRECTORY:` to invoke tasks within the included build, where `DIRECTORY`
   is the last path component of the included build's root directory.

See [discussions/6551](https://github.com/open-telemetry/opentelemetry-java/discussions/6551) for
known issues and solutions.

## Dev environment setup

### EditorConfig

An `.editorconfig` file is provided and automatically applied by IntelliJ to match project style.
It doesn't cover all formatting rules — `./gradlew spotlessApply` is still required — but it
reduces friction for common cases.

### Pre-commit hook

A pre-commit hook that runs `spotlessApply` automatically is provided at `buildscripts/pre-commit`.
Copy or symlink it into `.git/hooks/` to delegate formatting to the machine:

```bash
cp buildscripts/pre-commit .git/hooks/pre-commit
# or
ln -s ../../buildscripts/pre-commit .git/hooks/pre-commit
```

## Updating OTLP protobufs

OTLP protobuf Java bindings are published via
[opentelemetry-proto-java](https://github.com/open-telemetry/opentelemetry-proto-java). This
project uses the `.proto` files published in that binding jar, not the generated Java bindings
themselves.

When a new version of the upstream proto definitions is released, the process is:

1. [Release a new version of the java bindings](https://github.com/open-telemetry/opentelemetry-proto-java/blob/main/RELEASING.md)
   in the `opentelemetry-proto-java` repo (which we maintain).
2. Renovate will automatically open a PR to update `io.opentelemetry.proto:opentelemetry-proto`
   in this repo once the new version is published.
