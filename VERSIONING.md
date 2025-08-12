# OpenTelemetry Java Versioning

## Compatibility requirements

This codebase strictly follows [Semantic Versioning 2.0.0](https://semver.org/). This means
that all artifacts have a version of the format `MAJOR.MINOR.PATCH` or `MAJOR.MINOR.PATCH-alpha`.
For any artifact with a stable release, that is its version does not end in `-alpha`, no backwards-incompatible
changes will be made unless incrementing the `MAJOR` version number. In practice, this means that
backwards-incompatible changes will be avoided as long as possible. Most releases are made by
incrementing the `MINOR` version. Patch releases with urgent cherry-picked bugfixes will be made by
incrementing the `PATCH` version.

A backwards-incompatible change affects the public API of a module. The public API is any public
class or method that is not in a package which includes the word `internal`. Examples of incompatible
changes are:

- API changes that could require code using the artifact to be changed, e.g., removing a method,
  reordering parameters, adding a method to an interface or abstract class without adding a default
  implementation.

- [ABI](https://wikipedia.org/wiki/Application_binary_interface) changes that could require code using the artifact to be recompiled, but not changed, e.g.,
  changing the return type of a method from `void` to non-`void`, changing a `class` to an `interface`.
  The [JLS](https://docs.oracle.com/javase/specs/jls/se7/html/jls-13.html) has more information on
  what constitutes compatible changes.

- Behavior changes that can require code using the artifact to be changed, e.g., throwing an exception
  in code that previously could not. Note, the opposite is not true, replacing an exception with a
  no-op is acceptable if the no-op does not have a chance of causing errors in other parts of the
  application.

Such changes will be avoided - if they must be made, the `MAJOR` version of the artifact will be
incremented.

A stable artifact may depend on an `-alpha` artifact, and expose classes, interfaces, enums, etc. of
the `-alpha` artifact as part of its public API. In these cases, the stable artifact will place
an [implementation](https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation)
dependency (as opposed to an api dependency) on the `-alpha` artifact. In order to consume the
portions of the API related to the `-alpha` artifact, a user must place their own implementation
dependency on it. In adding the implementation dependency, the user has opted into to using
an `-alpha` artifact, and we reserve the right to change the portions of the API pertaining to
the `-alpha` artifact. This includes changing the names of methods, return types, argument types, etc.
We will use this technique sparingly and only when there is some significant reduction in friction
by including the `-alpha` artifact.

Backwards incompatible changes to `internal` packages are expected. Versions of published artifacts
are expected to be aligned by using BOMs we publish. We will always provide BOMs to allow alignment
of versions.

Changes may be made that require changes to the an app's dependency declarations aside from just
incrementing the version on `MINOR` version updates. For example, code may be separated out to a
new artifact which requires adding the new artifact to dependency declarations.

On rare occasions we may deprecate an entire stable artifact, with the intent of stopping functional
changes or enhancements. In these situations we may stop publishing additional `MINOR` or `MAJOR`
versions of the artifact. However, if necessary, we'll publish security fixes via `PATCH` releases.
The API of the last published version will remain stable.

As a user, if you always depend on the latest version of the BOM for a given `MAJOR` version, and
you do not use classes in the `internal` package (which you MUST NOT do), you can be assured that
your app will always function and have access to the latest features of OpenTelemetry without needing
any changes to code.

## Language Version Compatibility

The artifacts published by this codebase are compatible with certain language levels of tooling in
the Java ecosystem. For example, all artifacts (except where otherwise noted) support Java language
level 8 or higher, and the many artifacts intended to be used in Android environments adhere to a
particular [Android API level](https://developer.android.com/tools/releases/build-tools). The
following table defines the minimum language levels we adhere to, and how each is considered with
respect to semantic versioning.

| Language | Minimum Version                                                                                                                                                                                                                  | Applicability                                           | Semconv Notes                                                                                                                                                                      |
|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java     | 8+                                                                                                                                                                                                                               | All artifacts, unless otherwise noted                   | Changing requires major version bump.                                                                                                                                              |
| Android  | 23+ (NOTE: [desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) is required. We stay up to date with the latest version of [desugar_jdk_libs](https://github.com/google/desugar_jdk_libs).) | Artifacts using `otel.animalsniffer-conventions` plugin | Kept in sync with minimum requirements for [Google Play services](https://developers.google.com/android/guides/setup). Subject to change in minor version.                         |
| Kotlin   | 1.8+                                                                                                                                                                                                                             | Only applies to `opentelemetry-extension-kotlin`        | Kept in sync with [minimum non-deprecated](https://kotlinlang.org/docs/gradle-compiler-options.html#attributes-common-to-jvm-and-javascript) version. Subject to change in minor versions. |

## API vs SDK

This codebase is broadly split into two large pieces, the OpenTelemetry API and the OpenTelemetry SDK,
including extensions respectively. Until a `MAJOR` version bump, all artifacts in the codebase, both
for API and SDK, will be released together with identical `MAJOR.MINOR.PATCH` versions. If one of the
two has its `MAJOR` version incremented independently, for example SDK v2 is released while still
targeting API v1, then all artifacts in that category will be released together. The details for this
will be fleshed out at the time - it can be expected that the repository is split in some way to
ensure all artifacts within a single repository are at the same version number.

When incrementing the `MAJOR` version of the API, previously released `MAJOR` versions will be supported
for at least three more years. This includes

- No backwards incompatible changes, as defined above
- A version of the SDK supporting the latest minor version of this API will be maintained
- Bug and security fixes will be backported.
- Additional features generally will not be backported

When incrementing the `MAJOR` version of the SDK, previously released `MAJOR` versions will be supported
for at least one year.

## Stable vs alpha

Not all of our artifacts are published as stable artifacts - any non-stable artifact has the suffix
`-alpha` on its version. NONE of the guarantees described above apply to alpha artifacts. They may
require code or environment changes on every release and are not meant for consumption for users
where versioning stability is important.

When an alpha artifact is ready to be made stable, the next release will be made as usual by bumping
the minor version, while the `-alpha` suffix will be removed. For example, if the previous release
of `opentelemetry-sdk` is `1.2.0` and of `opentelemetry-sdk-metrics` is `1.2.0-alpha`, the next
release when making metrics stable will have both artifacts with the version `1.3.0`. Notably,
`MAJOR` versions are only used to indicate a backwards-incompatible change and are not used to
announce new features.
