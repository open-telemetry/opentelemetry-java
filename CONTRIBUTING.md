# Contributing

Welcome to OpenTelemetry Java repository!

Before you start - see OpenTelemetry general
[contributing](https://github.com/open-telemetry/community/blob/main/guides/contributor/README.md)
requirements and recommendations.

If you want to add new features or change behavior, please make sure your changes follow the
[OpenTelemetry Specification](https://github.com/open-telemetry/opentelemetry-specification).
Otherwise, file an issue or submit a pull request (PR) to the specification repo first.

Make sure to review the projects [license](LICENSE) and sign the
[CNCF CLA](https://identity.linuxfoundation.org/projects/cncf). A signed CLA will be enforced by an
automatic check once you submit a PR, but you can also sign it after opening your PR.

## Requirements

Java 21 or higher is required to build the projects in this repository. The built artifacts can be
used on Java 8 or higher.

## Building opentelemetry-java

Continuous integration builds the project, runs the tests, and runs multiple types of static
analysis.

1. Note: Currently, to run the full suite of tests, you'll need to be running a docker daemon. The
   tests that require docker are disabled if docker is not present. If you wish to run them, you
   must run a local docker daemon.

2. Clone the repository

   `git clone https://github.com/open-telemetry/opentelemetry-java.git`

3. Run the following commands to build, run tests and most static analysis, and check formatting:

   `./gradlew build`

4. If you are a Windows user, use the alternate command mentioned below to run tests and check
   formatting:

   `gradlew.bat`

## Checks

Before submitting a PR, you should make sure the style checks and unit tests pass. You can run these
with the `check` task.

```bash
$ ./gradlew check
```

Note: this gradle task will potentially generate changes to files in
the `docs/apidiffs/current_vs_latest`
directory. Please make sure to include any changes to these files in your pull request (i.e.
add those files to your commits in the PR).

## PR Review

After you submit a PR, it will be reviewed by the project maintainers and approvers. Not all
maintainers need to review a particular PR, but merging to the base branch is authorized to
restricted members (administrators).

### Draft PRs

Draft PRs are welcome, especially when exploring new ideas or experimenting with a hypothesis.
However, draft PRs may not receive the same degree of attention, feedback, or scrutiny unless
requested directly. In order to help keep the PR backlog maintainable, drafts older than 6 months
will be closed by the project maintainers. This should not be interpreted as a rejection. Closed
PRs may be reopened by the author when time or interest allows.

## Project Scope

`opentelemetry-java` is one of several repositories which comprise the OpenTelemetry Java ecosystem,
and contains the core components upon which instrumentation and extensions are built. In order to
prevent sprawl and maintain a high level of quality, we limit this repository's scope to components
which implement concepts defined in
the [opentelemetry-specification](https://github.com/open-telemetry/opentelemetry-specification),
with a few exceptions / comments:

* The [API incubator](./api/incubator) and [SDK incubator](./sdk-extensions/incubator)
  contain prototypes which have been discussed in the specification
  or [oteps](https://github.com/open-telemetry/oteps) and have a reasonable chance of becoming part
  of the specification, subject to maintainers' discretion.
* Components like the [Kotlin Extension](./extensions/kotlin) are included which are required for
  the API / SDK to function in key areas of the Java ecosystem. Inclusion is subject to maintainers'
  discretion.
* As a general rule, components which implement semantic conventions belong elsewhere.

Other repositories in the OpenTelemetry Java ecosystem include:

* [opentelemetry-java-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
  contains instrumentation.
* [opentelemetry-java-contrib](https://github.com/open-telemetry/opentelemetry-java-contrib)
  contains extensions, prototypes, and instrumentation, including vendor specific components.
* [opentelemetry-java-examples](https://github.com/open-telemetry/opentelemetry-java-examples) contains
  working code snippets demonstrating various concepts.

## Style guideline

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). Our
build will fail if source code is not formatted according to that style. To fix any style failures
the above [checks](#checks) show, automatically apply the formatting with:

```bash
$ ./gradlew spotlessApply
```

To verify code style manually run the following command, which
uses [google-java-format](https://github.com/google/google-java-format) library:

`./gradlew spotlessCheck`

### Best practices that we follow

* This project uses [semantic versioning](https://semver.org/). Except for major versions, a user
  should be able to update their dependency version on this project and have nothing break. This
  means we do not make breaking changes to the API (e.g., remove a public method) or to the ABI (
  e.g., change return type from void to non-void).
* Avoid exposing publicly any class/method/variable that don't need to be public.
* By default, all arguments/members are treated as non-null. Every argument/member that can
  be `null` must be annotated with `@Nullable`.
* The project aims to provide a consistent experience across all the public APIs. It is important to
  ensure consistency (same look and feel) across different public packages.
* Use `final` for public classes everywhere it is possible, this ensures that these classes cannot
  be extended when the API does not intend to offer that functionality.
* In general, we use the following ordering of class members:
  * Static fields (final before non-final)
  * Instance fields (final before non-final)
  * Constructors
    * In static utility classes (where all members are static), the private constructor
      (used to prevent construction) should be ordered after methods instead of before methods.
  * Methods
    * If methods call each other, it's nice if the calling method is ordered (somewhere) above the
      method that it calls. So, for one example, a private method would be ordered (somewhere) below
      the non-private methods that use it.
  * Nested classes
* Adding `toString()` overrides on classes is encouraged, but we only use `toString()` to provide
  debugging assistance. The implementations of all `toString()` methods should be considered to be
  unstable unless explicitly documented otherwise.
* Avoid synchronizing using a class's intrinsic lock. Instead, synchronize on a dedicated lock object. E.g:
  ```java
  private final Object lock = new Object();

  public void doSomething() {
    synchronized (lock) { ... }
  }
  ```
* Don't
  use [gradle test fixtures](https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures) (
  i.e. `java-test-fixtures` plugin) to reuse code for internal testing. The test fixtures plugin has
  side effects where test dependencies are added to the `pom.xml` and publishes an
  extra `*-test-fixtures.jar` artifact which is unnecessary for internal testing. Instead, create a
  new `*:testing-internal` module and omit the `otel.java-conventions`. For example,
  see [/exporters/otlp/testing-internal](./exporters/otlp/testing-internal).

If you notice any practice being applied in the project consistently that isn't listed here, please
consider a pull request to add it.

### Pre-commit hook

To completely delegate code style formatting to the machine, you can
add [git pre-commit hook](https://git-scm.com/docs/githooks). We provide an example script
in `buildscripts/pre-commit` file. Just copy or symlink it into `.git/hooks` folder.

### Editorconfig

As additional convenience for IntelliJ Idea users, we provide `.editorconfig` file. Idea will
automatically use it to adjust its code formatting settings. It does not support all required rules,
so you still have to run `spotlessApply` from time to time.

### Javadoc

* All public classes and their public and protected methods MUST have javadoc. It MUST be complete (
  all params documented etc.) Everything else
  (package-protected classes, private) MAY have javadoc, at the code writer's whim. It does not have
  to be complete, and reviewers are not allowed to require or disallow it.
* Each API element should have a `@since` tag specifying the minor version when it was released (or
  the next minor version).
* There MUST be NO javadoc errors.

See [section 7.3.1](https://google.github.io/styleguide/javaguide.html#s7.3.1-javadoc-exception-self-explanatory)
in the guide for exceptions to the Javadoc requirement.

* Reviewers may request documentation for any element that doesn't require Javadoc, though the style
  of documentation is up to the author.
* Try to do the least amount of change when modifying existing documentation. Don't change the style
  unless you have a good reason.
* We do not use `@author` tags in our javadoc.
* Our javadoc is available via [
  javadoc.io}(https://javadoc.io/doc/io.opentelemetry/opentelemetry-api)

### SDK Configuration Documentation

All changes to the SDK configuration options or autoconfigure module should be documented on
[opentelemetry.io](https://opentelemetry.io/docs/languages/java/configuration/).

### AutoValue

* Use [AutoValue](https://github.com/google/auto/tree/master/value), when possible, for any new
  value classes. Remember to add package-private constructors to all AutoValue classes to prevent
  classes in other packages from extending them.

### Unit Tests

* Unit tests target Java 8, so language features such as lambda and streams can be used in tests.

## Specific tasks

### Updating the Snapshot build number

The overall version number for opentelemetry-java is determined from git tags, and not fixed in any
file.

This means it will not update, even if you `git pull` from the repo tip. It will still produce a set
of libraries with the old version number.

To update it, you must fetch the tags, via `git fetch --all --tags` - which should work, even if you
have forked the repo, as long as the trunk repo is set as an upstream remote.

### Composing builds

Beware that this section is only meant for developers of opentelemetry-java, or closely related
projects. The steps described here could change at any time and what you do for one version (commit)
may break with the next one already.

Gradle provides a feature
called ["composite builds"](https://docs.gradle.org/current/userguide/composite_builds.html)
that allows to replace some normally externally provided dependencies with a project that is built
(included) in the same Gradle invocation. This can be useful to quickly test a new feature or bug
fix you are developing in opentelemetry-java with the examples or the app or instrumentation library
where you need the feature or run into the bug. Unfortunately, opentelemetry-java does not work out
of the box with this feature because Gradle is unable to map the project names to the customized
artifact coordinates (see e.g. [gradle/gradle#18291](https://github.com/gradle/gradle/issues/18291)
and related issues. However, gradle supports manually declaring the mapping between ("substitution
of")
artifact coordinates and project names. To ease this tedious task, opentelemetry-java provides a
gradle task `:generateBuildSubstitutions` that generates a code snippet with these substitutions in
kts (Kotlin Script) format.

Example usage could be as follows:

1. Run `./gradlew generateBuildSubstitutions`
2. Two files named `build/substitutions.gradle.kts` are generated in the bom and bom-alpha project's
   directory, containing substitutions for the stable and alpha projects respectively.
3. Copy & paste the content of these files to a new `settings.gradle.kts` or the one where you want
   to include the opentelemetry build into, so that it contains something like the following:

   ```kotlin
   includeBuild("PATH/TO/OPENTELEMETRY-JAVA/ROOT/DIRECTORY") {
     // Copy & paste following block from the generated substitutions.gradle.kts, *not* from here!
     dependencySubstitution {
       substitute(module("io.opentelemetry:opentelemetry-api")).using(project(":api:all"))
       substitute(module("io.opentelemetry:opentelemetry-sdk")).using(project(":sdk:all"))
       // ...
     }
   }
   ```

   Please confirm whether the local opentelemetry-java version is consistent with the
   opentelemetry-java version declared in the project that relies on opentelemetry-java.
   If it is inconsistent, `dependencySubstitution` may not take effect.

   See [the Gradle documentation](https://docs.gradle.org/current/userguide/composite_builds.html#included_build_declaring_substitutions)
   for more information.
4. If you now build your project, it will use the included build to supply the opentelemetry-java
   artifacts, ignoring any version declarations. Use the prefix `:DIRECTORY:` to refer to
   tasks/projects within the included build, where DIRECTORY is the name of the directory in the
   included build (only the part after the last `/`).
5. Here are some issues and solutions ([discussions/6551](https://github.com/open-telemetry/opentelemetry-java/discussions/6551))
   you may encounter that may be helpful to you.

### Updating the OTLP protobufs

OTLP protobuf Java bindings are published via
the [opentelemetry-proto-java](https://github.com/open-telemetry/opentelemetry-proto-java)
repository. This project does not use the java bindings, but does use the `.proto` files that are
published in the binding jar by that project.

To update the OTLP protobuf version,
first [release a new version of the java bindings](https://github.com/open-telemetry/opentelemetry-proto-java/blob/main/RELEASING.md)
then simply update the dependency version that this project has on that jar.
