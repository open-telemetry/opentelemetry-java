# API Design

See [VERSIONING.md](../../VERSIONING.md) for the full versioning and compatibility policy.

## Breaking changes in alpha modules

Breaking changes are allowed in alpha modules but should still be approached carefully:

- Prefer going through a deprecation cycle to ease migration, unless the deprecation would be too
  cumbersome or the old API is genuinely unusable.
- Bundle breaking changes together where possible to reduce churn for consumers.
- Deprecations in alpha modules are typically introduced and removed within one release cycle
  (approximately one month).

## Breaking changes in stable modules

Breaking changes are not allowed in stable modules outside of a major version bump. The build
enforces this via japicmp — see [japicmp](#japicmp) below.

## Deprecating API

Applies to both stable and alpha modules. Use plain `@Deprecated` — do **not** use
`forRemoval = true` or `since = "..."` (Java 8 compatibility requirement).

```java
/**
 * @deprecated Use {@link #newMethod()} instead.
 */
@Deprecated
public ReturnType oldMethod() {
  return newMethod(); // delegate to replacement
}
```

Rules:
- Include a `@deprecated` Javadoc tag naming the replacement.
- The deprecated method must delegate to its replacement, not the other way around — this ensures
  overriders of the old method still get called.
- Deprecated items in stable modules cannot be removed until the next major version.
- Deprecated items in alpha modules should indicate when they will be removed (e.g.
  `// to be removed in 1.X.0`). It is also common to log a warning when a deprecated-for-removal
  feature is used, to increase visibility for consumers.
- Add the `deprecation` label to the PR.

## japicmp

`otel.japicmp-conventions` runs the `jApiCmp` task as part of `check` for every published module.
It compares the locally-built jar against the latest release to detect breaking changes, and writes
a human-readable diff to `docs/apidiffs/current_vs_latest/<artifact>.txt`.

- Breaking changes in stable modules fail the build.
- AutoValue classes are exempt from the abstract-method-added check — the generated implementation
  handles those automatically.
- The diff files are committed to the repo. Include any changes to them in your PR.
- Run `./gradlew jApiCmp` to regenerate diffs after API changes.

## Null guards

`@Nullable` annotations (see [general-patterns.md](general-patterns.md)) and
[NullAway](https://github.com/uber/NullAway) enforce null contracts at build time within this
repo. At runtime there is no such guarantee — callers in other
codebases can pass `null` regardless of annotations. Add null guards only at **public API entry
points**; once inside the implementation, trust NullAway.

### Configuration-time boundaries (SDK builders, provider factories)

Fail fast with `Objects.requireNonNull`:

```java
public SdkTracerProviderBuilder setResource(Resource resource) {
  Objects.requireNonNull(resource, "resource");
  this.resource = resource;
  return this;
}
```

These APIs are called once during startup, so a hard failure surfaces the bug immediately and
unambiguously.

### Runtime / instrumentation-time boundaries (Span methods, metric recordings, log builders)

Do **not** throw. Log the violation via
[`ApiUsageLogger`](../../common/src/main/java/io/opentelemetry/common/ApiUsageLogger.java) —
which logs at `FINEST` with a stack trace so the offending call site is visible — then degrade
gracefully (return `this`, an empty/noop result, or substitute a safe default such as
`Attributes.empty()` or `Context.current()`):

```java
@Override
public Span addEvent(String name) {
  if (name == null) {
    ApiUsageLogger.logNullParam(Span.class, "addEvent", "name");
    return this;
  }
  // ... normal implementation
}
```

The class and method arguments identify the problem immediately in the log message without
requiring stack trace analysis. Use `ApiUsageLogger.log(...)` directly when the message is not
simply "X is null" (e.g. `"spanIdBytes is null or too short"`). `FINEST` is silent by default, so there is no production noise.
To investigate misuse, enable the logger named `io.opentelemetry.usage` at `FINEST` in
development, or periodically in staging/production. Check each argument once, at the first
public entry point — internal methods called by that entry point do not need to re-validate.

### SDK extension interfaces and SPIs

These interfaces are called by the SDK, not directly by application developers.
Examples include `Sampler`, `SpanExporter`, `SpanProcessor`, `LogRecordExporter`,
`MetricExporter`, `MetricReader`, `ComponentProvider`, and SPI interfaces such as those in
`sdk-extensions/autoconfigure-spi` (`ResourceProvider`, `AutoConfigurationCustomizerProvider`,
etc.), `HttpSenderProvider`, and `ContextStorageProvider`.

Because the SDK is NullAway-verified, a null argument here indicates a bug in the SDK itself,
not misuse by an application developer. Use `Objects.requireNonNull` — a hard failure surfaces
the bug immediately and unambiguously, which is preferable to silent degradation that would
mask the underlying SDK defect.

### Where to implement guards

Add guards in the concrete implementation class, or in an existing `default` interface method
that would otherwise NPE. Do **not** add new `default` methods to interfaces solely for null
safety — that expands the interface surface without a functional benefit.

## Stable vs alpha modules

Artifacts without an `-alpha` version suffix are **stable**. Artifacts with `-alpha` have no
compatibility guarantees and may break on every release. The `internal` package is always exempt
from compatibility guarantees regardless of artifact stability.

To mark a module as alpha, add a `gradle.properties` file at the module root containing:

```properties
otel.release=alpha
```

See [`exporters/prometheus/gradle.properties`](../../exporters/prometheus/gradle.properties) for
an example.

See [VERSIONING.md](../../VERSIONING.md) for the full compatibility policy, including what
constitutes a source-incompatible vs binary-incompatible change.
