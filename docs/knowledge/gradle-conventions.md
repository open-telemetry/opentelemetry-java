# Gradle Conventions

## AutoValue in build files

See [general-patterns.md](general-patterns.md#autovalue) for the AutoValue usage pattern.
`auto-value-annotations` is provided globally by `otel.java-conventions`. Only add the
annotation processor if the module uses AutoValue in production code:

```kotlin
dependencies {
  annotationProcessor("com.google.auto.value:auto-value")        // production
  testAnnotationProcessor("com.google.auto.value:auto-value")    // tests only
}
```

## Convention plugins

Every module applies a base set of convention plugins from `buildSrc/src/main/kotlin/`.

| Plugin | Purpose | Who applies it |
| --- | --- | --- |
| `otel.java-conventions` | Base Java toolchain, Checkstyle, Spotless, Error Prone, test config | All modules |
| `otel.publish-conventions` | Maven publishing, POM generation | Published (non-internal) modules |
| `otel.animalsniffer-conventions` | Android API level compatibility checking | Modules targeting Android |
| `otel.jmh-conventions` | JMH benchmark support | Modules with benchmarks |
| `otel.japicmp-conventions` | API diff generation against latest release | Published modules (applied by `otel.publish-conventions`) |
| `otel.protobuf-conventions` | Protobuf code generation | Protobuf modules only |

A typical published module:

```kotlin
plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")
}
```

A testing-internal module (shared test utilities, not published):

```kotlin
plugins {
  id("otel.java-conventions")
  // no otel.publish-conventions
}
```

## Dependency resolution

All external dependency versions are managed by `:dependencyManagement` (a BOM). Do not
specify versions directly in `build.gradle.kts` — add new entries to the BOM if a version
needs to be pinned.

`otel.java-conventions` configures `failOnVersionConflict()` and `preferProjectModules()`
globally. Do not override these without a strong reason.

## Module naming

Every module must declare its Java module name:

```kotlin
otelJava.moduleName.set("io.opentelemetry.sdk.metrics")
```

When the archive name cannot be derived correctly from the project name, set it explicitly:

```kotlin
base.archivesName.set("opentelemetry-api")
```

## `settings.gradle.kts` ordering

New `include(...)` entries must be in **alphabetical order** within their surrounding group. The
file is large — find the right lexicographic position before inserting.

## Shared test utilities and test suites

See [testing-patterns.md](testing-patterns.md) for shared test utility patterns and test suite
registration.
