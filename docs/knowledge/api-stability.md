# API Stability and Breaking Changes

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
