# Contributing

We welcome contributions to this repository!

Have a look at our [community repo](https://github.com/open-telemetry/community) to get started with OpenTelemetry.

If you want to add new features or change behavior, please make sure your changes follow the [OpenTelemetry Specification](https://github.com/open-telemetry/opentelemetry-specification).
Otherwise file an issue or submit a PR to the specification repo first.

Make sure to review the projects [license](LICENSE) and sign the [CNCF CLA](https://identity.linuxfoundation.org/projects/cncf).
A signed CLA will be enforced by an automatic check once you submit a PR, but you can also sign it after opening your PR.

See our general [contribution guidelines](https://github.com/open-telemetry/community/blob/master/CONTRIBUTING.md) for more information.

## Style guideline

We follow the [Google Java Style
Guide](https://google.github.io/styleguide/javaguide.html). Our build
automatically will provide warnings for simple style issues.

Run the following command to format all files. This formatter uses
[google-java-format](https://github.com/google/google-java-format):

### OS X or Linux

`./gradlew goJF`

### Windows

`gradlew.bat goJF`

We also follow these project-specific guidelines:

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

``` sh
$ git checkout -b docs
$ ./gradlew javadoc
$ rm -fr docs/*
$ cp -R api/build/docs/javadoc/* docs
$ git add -A .
$ git commit -m "Update javadoc for API."
```

### AutoValue

* Use [AutoValue](https://github.com/google/auto/tree/master/value), when
  possible, for any new value classes. Remember to add package-private
  constructors to all AutoValue classes to prevent classes in other packages
  from extending them.

## Building opentelemetry-java

Continuous integration builds the project, runs the tests, and runs multiple
types of static analysis.

Before running the build init repository dependencies by running `make init-git-submodules`.

Run the following commands to build, run tests and most static analysis, and
check formatting:

### OS X or Linux

`make test verify-format`

### Windows

`gradlew.bat clean assemble check verGJF`
