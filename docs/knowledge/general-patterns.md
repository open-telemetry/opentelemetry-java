# General Patterns

## @Nullable

All arguments and members are treated as non-null by default. Annotate with `@Nullable` (from
`javax.annotation`) only when `null` is actually possible.

- **Fields**: annotate only if the field can hold `null` after construction.
- **Parameters**: annotate only if `null` is actually passed by callers.
- **Return types**: annotate only if the method actually returns `null`. A non-null implementation
  of a `@Nullable`-declared interface method should omit the annotation — it is more precise.

For null guard behavior at public API boundaries, see [api-design.md](api-design.md).

## API consistency

The project aims to provide a consistent experience across all public APIs. When designing new
API, look for prior art in the project before introducing new patterns — prefer the style already
established in the same package, then the same module, then the broader project.

## AutoValue

Use [AutoValue](https://github.com/google/auto/tree/master/value) for new value classes.

- Add `annotationProcessor("com.google.auto.value:auto-value")` in `build.gradle.kts`.
- `auto-value-annotations` is already available via `otel.java-conventions`.
- Add a package-private constructor to all AutoValue classes to prevent external extension.
- `japicmp` allows new abstract methods on AutoValue classes — the `AllowNewAbstractMethodOnAutovalueClasses` rule handles this.

## Class member ordering

In general, order class members as follows:

1. Static fields (final before non-final)
2. Instance fields (final before non-final)
3. Constructors
   - In static utility classes, the private constructor goes after methods, not before.
4. Methods
   - If methods call each other, the calling method should appear above the method it calls.
5. Nested classes

## Dedicated lock objects

Do not synchronize using a class's intrinsic lock (`synchronized(this)` or `synchronized` method).
Use a dedicated lock object:

```java
private final Object lock = new Object();

public void doSomething() {
  synchronized (lock) { ... }
}
```

## Formatting

Formatting is enforced by three tools. Run `./gradlew spotlessApply` before committing — it fixes
most violations automatically.

- **Spotless** — formatting rules vary by file type (see `buildSrc/src/main/kotlin/otel.spotless-conventions.gradle.kts`):
  Java uses google-java-format; Kotlin uses ktlint. Also enforces the Apache license header
  (template in `buildscripts/spotless.license.java`) and misc file hygiene (trailing whitespace,
  final newline, etc.).
- **Checkstyle** — enforces naming conventions, import ordering, Javadoc structure, and other
  rules not covered by formatting alone. Config is in `buildscripts/checkstyle.xml`.
- **EditorConfig** (`.editorconfig`) — configures IntelliJ to match project style automatically.
  It doesn't cover all rules, so `spotlessApply` is still required.

## Impl (Implementation) code

Use `*.impl.*` sub-packages for code that must be `public` to be shared across OpenTelemetry
modules, but is not intended for use by application developers. This is the right choice when
`*.internal.*` is too restrictive — for example, a utility used by both the API and SDK modules
that needs a stable contract across module boundaries.

Unlike `*.internal.*` packages, `*.impl.*` packages carry full backwards-compatibility guarantees
and are included in `japicmp` compatibility checks.

Public classes in `impl` packages must carry the following disclaimer (enforced by the
`OtelImplJavadoc` Error Prone check in `custom-checks/`):

```java
/**
 * This class is not intended for use by application developers. Its API is stable and will not
 * be changed or removed in a backwards-incompatible manner.
 */
```

See also [Internal code](#internal-code) for code that is not for application developers and has
no stability guarantees.

## Internal code

Prefer package-private over putting code in an `internal` package. Use `internal` only when the
code must be `public` for technical reasons (e.g. accessed across packages within the same module)
but should not be part of the public API.

Public classes in `internal` packages are excluded from semver guarantees and Javadoc, but they
must carry one of the two standard disclaimers (enforced by the `OtelInternalJavadoc` Error Prone
check in `custom-checks/`):

```java
// Standard internal disclaimer:
/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */

// For incubating internal code that may be promoted to public API:
/**
 * This class is internal and experimental. Its APIs are unstable and can change at any time.
 * Its APIs (or a version of them) may be promoted to the public stable API in the future, but
 * no guarantees are made.
 */
```

Internal code must not be used across module boundaries — module `foo` must not call internal
code from module `bar`. Cross-module internal usage is a known issue being tracked and cleaned up
in open-telemetry/opentelemetry-java#6970.

See also [Impl (Implementation) code](#impl-implementation-code) for cross-module code that is not for application developers but
requires a stable API.

## Javadoc

All public classes, and their public and protected methods, must have complete Javadoc including
all parameters — this is enforced for public APIs. Package-private and private members may have
Javadoc at the author's discretion.

- No `@author` tags.
- New public API elements require a `@since` annotation. This is added automatically during the
  [release process](../../RELEASING.md) — do not include it in your PR.
- See [section 7.3.1](https://google.github.io/styleguide/javaguide.html#s7.3.1-javadoc-exception-self-explanatory)
  for self-explanatory exceptions.

Published Javadoc is available at https://javadoc.io/doc/io.opentelemetry.

## Language version compatibility

Production code targets Java 8. Test code also targets Java 8. Do not use Java 9+ APIs unless
the module explicitly sets a higher minimum. See [VERSIONING.md](../../VERSIONING.md) for the full
language version compatibility policy, including Android and Kotlin minimum versions.

## Logging

Use `java.util.logging` (JUL) in production source sets. Do not use SLF4J or other logging
frameworks in `src/main/`. Tests bridge JUL to SLF4J via `JulBridgeInitializer` (configured
automatically by `otel.java-conventions`).

```java
private static final Logger LOGGER = Logger.getLogger(MyClass.class.getName());
```

When logging exceptions, pass the exception as the `Throwable` parameter to the logger rather
than stringifying it via `getMessage()` or concatenation. This ensures logging frameworks can
render the full stack trace.

```java
// Do:
logger.log(Level.WARNING, "Failed to process request", exception);
// Don't:
logger.warning("Failed to process request: " + exception.getMessage());
```

## toString()

Adding `toString()` overrides is encouraged for debugging assistance. All `toString()`
implementations should be considered unstable unless explicitly documented otherwise — do not
rely on their output programmatically.

## Visibility

Use the most restrictive access modifier that still allows the code to function correctly. Use
`final` on classes unless extension is explicitly intended.
