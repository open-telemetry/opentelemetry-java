# Contributing

Welcome to OpenTelemetry Java repository!

Before you start - see OpenTelemetry general
[contributing](https://github.com/open-telemetry/community/blob/main/CONTRIBUTING.md)
requirements and recommendations.

If you want to add new features or change behavior, please make sure your changes follow the
[OpenTelemetry Specification](https://github.com/open-telemetry/opentelemetry-specification).
Otherwise file an issue or submit a PR to the specification repo first.

Make sure to review the projects [license](LICENSE) and sign the
[CNCF CLA](https://identity.linuxfoundation.org/projects/cncf). A signed CLA will be enforced by an
automatic check once you submit a PR, but you can also sign it after opening your PR.

## Requirements

Java 11 or higher is required to build the projects in this repository. The built artifacts can be
used on Java 8 or higher.

## Building opentelemetry-java

Continuous integration builds the project, runs the tests, and runs multiple
types of static analysis.

1. Note: Currently, to run the full suite of tests, you'll need to be running a docker daemon.
The tests that require docker are disabled if docker is not present. If you wish to run them,
you must run a local docker daemon.

2. Clone the repository

    `git clone https://github.com/open-telemetry/opentelemetry-java.git`

3. Run the following commands to build, run tests and most static analysis, and
check formatting:

    `./gradlew build`

4. If you are a Windows user, use the alternate command mentioned below to run tests and
check formatting:

     `gradlew.bat`

## Checks

Before submitting a PR, you should make sure the style checks and unit tests pass. You can run these
with the `check` task.

```bash
$ ./gradlew check
```

Note: this gradle task will potentially generate changes to files in the `docs/apidiffs/current_vs_latest`
directory. Please make sure to include any changes to these files in your pull request.

## PR Review
After you submit a PR, it will be reviewed by the project maintainers and approvers. Not all maintainers need to review a
particular PR, but merging to the base branch is authorized to restricted members (administrators).

## Style guideline

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
Our build will fail if source code is not formatted according to that style. To fix any
style failures the above [checks](#checks) show, automatically apply the formatting with:

```bash
$ ./gradlew spotlessApply
```

To verify code style manually run the following command,
which uses [google-java-format](https://github.com/google/google-java-format) library:

`./gradlew spotlessCheck`

### Best practices that we follow

* This project uses [semantic versioning](https://semver.org/). Except for major versions, a user should be able to update
their dependency version on this project and have nothing break. This means we do not make breaking
changes to the API (e.g., remove a public method) or to the ABI (e.g., change return type from void to non-void).
* Avoid exposing publicly any class/method/variable that don't need to be public.
* By default, all arguments/members are treated as non-null. Every argument/member that can be `null` must be annotated with `@Nullable`.
* The project aims to provide a consistent experience across all the public APIs. It is important to ensure consistency (same look and feel) across different public packages.
* Use `final` for public classes everywhere it is possible, this ensures that these classes cannot be extended when the API does not intend to offer that functionality.
* In general, we use the following ordering of class members:
    * Static fields (final before non-final)
    * Instance fields (final before non-final)
    * Constructors
      * In static utility classes (where all members are static), the private constructor
        (used to prevent construction) should be ordered after methods instead of before methods.
    * Methods
      * If methods call each other, it's nice if the calling method is ordered (somewhere) above
        the method that it calls. So, for one example, a private method would be ordered (somewhere) below
        the non-private methods that use it.
    * Nested classes
* Adding `toString()` overrides on classes is encouraged, but we only use `toString()` to provide debugging assistance. The implementations
of all `toString()` methods should be considered to be unstable unless explicitly documented otherwise.

If you notice any practice being applied in the project consistently that isn't listed here, please consider a pull request to add it.

### Pre-commit hook
To completely delegate code style formatting to the machine,
you can add [git pre-commit hook](https://git-scm.com/docs/githooks).
We provide an example script in `buildscripts/pre-commit` file.
Just copy or symlink it into `.git/hooks` folder.

### Editorconfig
As additional convenience for IntelliJ Idea users, we provide `.editorconfig` file.
Idea will automatically use it to adjust its code formatting settings.
It does not support all required rules, so you still have to run `spotlessApply` from time to time.

### Javadoc

* All public classes and their public and protected methods MUST have javadoc.
  It MUST be complete (all params documented etc.) Everything else
  (package-protected classes, private) MAY have javadoc, at the code writer's
  whim. It does not have to be complete, and reviewers are not allowed to
  require or disallow it.
* Each API element should have a `@since` tag specifying the minor version when
  it was released (or the next minor version).
* There MUST be NO javadoc errors.
* See [section
  7.3.1](https://google.github.io/styleguide/javaguide.html#s7.3.1-javadoc-exception-self-explanatory)
  in the guide for exceptions to the Javadoc requirement.
* Reviewers may request documentation for any element that doesn't require
  Javadoc, though the style of documentation is up to the author.
* Try to do the least amount of change when modifying existing documentation.
  Don't change the style unless you have a good reason.
* We do not use `@author` tags in our javadoc.
* Our javadoc is available via [javadoc.io}(https://javadoc.io/doc/io.opentelemetry/opentelemetry-api)

### AutoValue

* Use [AutoValue](https://github.com/google/auto/tree/master/value), when
  possible, for any new value classes. Remember to add package-private
  constructors to all AutoValue classes to prevent classes in other packages
  from extending them.


### Unit Tests

* Unit tests target Java 8, so language features such as lambda and streams can be used in tests.

## Specific tasks

### Updating OTLP proto dependency version

The OTLP proto dependency version is defined [here](proto/build.gradle). To bump the version,

1. Find the latest release version [here](https://github.com/open-telemetry/opentelemetry-proto/releases/latest)
2. Download the zip source code archive
3. Run `shasum -a 256 ~/path/to/downloaded.zip` to compute its checksum
4. Update `protoVersion` and `protoChecksum` in the build file with the new version and checksum

### Composing builds

Beware that this section is only meant for developers of opentelemetry-java, or closely related projects.
The steps described here could change at any time and what you do for one version (commit) may break
with the next one already.

Gradle provides a feature called ["composite builds"](https://docs.gradle.org/current/userguide/composite_builds.html)
that allows to replace some normally externally provided dependencies with a project that is built
(included) in the same Gradle invocation. This can be useful to quickly test a new feature or bug fix you are
developing in opentelemetry-java with the examples or the app or instrumentation library where you
need the feature or run into the bug. Unfortunately, opentelemetry-java does not work out of the box
with this feature because Gradle is unable to map the project names to the customized artifact
coordinates (see e.g. [gradle/gradle#18291](https://github.com/gradle/gradle/issues/18291)
and related issues. However, gradle supports manually declaring the mapping between ("substitution of")
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

   See [the Gradle documentation](https://docs.gradle.org/current/userguide/composite_builds.html#included_build_declaring_substitutions)
   for more information.

4. If you now build your project, it will use the included build to supply the opentelemetry-java artifacts,
   ignoring any version declarations. Use the prefix `:DIRECTORY:` to refer to tasks/projects within
   the included build, where DIRECTORY is the name of the directory in the included build (only the part after the last `/`).
