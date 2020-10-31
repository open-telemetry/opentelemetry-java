# Contributing

Welcome to OpenTelemetry Java repository!

Before you start - see OpenTelemetry general
[contributing](https://github.com/open-telemetry/community/blob/master/CONTRIBUTING.md)
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

## Checks

Before submitting a PR, you should make sure the style checks and unit tests pass. You can run these
with the `check` task.

```bash
$ ./gradlew check
```

## Style guideline

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). 
Our build will fail if source code is not formatted according to that style. To fix any
style failures the above [checks](#checks) show, automatically apply the formatting with:

```bash
$ ./gradlew spotlessApply
```

To verify code style manually run the following command, 
which uses [google-java-format](https://github.com/google/google-java-format) library:

`./gradlew spotless`

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
* Our javadoc is available via [javadoc.io}(https://javadoc.io/doc/io.opentelemetry/opentelemetry-api)

### AutoValue

* Use [AutoValue](https://github.com/google/auto/tree/master/value), when
  possible, for any new value classes. Remember to add package-private
  constructors to all AutoValue classes to prevent classes in other packages
  from extending them.
  
  
### Unit Tests

* Unit tests target Java 8, so language features such as lambda and streams can be used in tests.


## Building opentelemetry-java

Continuous integration builds the project, runs the tests, and runs multiple
types of static analysis.

1. Note: Currently, to run the full suite of tests, you'll need to be running a docker daemon.
The tests that require docker are disabled by default. If you wish to run them,
you can enable the docker tests by setting a gradle property of
``"enable.docker.tests"`` to true. See the gradle.properties file in the root of the project
for more details.

2. Clone the repository recursively

    `git clone https://github.com/open-telemetry/opentelemetry-java.git --recursive`

or alternatively initialize submodules for an existing clone.

   `git submodule init`
   `git submodule update`   

3. Run the following commands to build, run tests and most static analysis, and
check formatting:

    `./gradlew check`
