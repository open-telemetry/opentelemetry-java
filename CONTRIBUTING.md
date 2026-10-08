# Contributing

Welcome to the OpenTelemetry Java repository!

Before you start, see the OpenTelemetry general
[contributing](https://github.com/open-telemetry/community/blob/main/guides/contributor/README.md)
requirements and recommendations.

## CLA and license

Review the project [license](LICENSE) and sign the
[CNCF CLA](https://identity.linuxfoundation.org/projects/cncf). A signed CLA will be enforced by
an automatic check once you submit a PR, but you can also sign it after opening your PR.

## Build and coding patterns

See [docs/knowledge/README.md](docs/knowledge/README.md) for coding patterns, build conventions,
testing guidance, and API stability rules. Build requirements and common commands are in
[docs/knowledge/build.md](docs/knowledge/build.md).

## PR review

After you submit a PR, it will be reviewed by the project maintainers and approvers. Not all
maintainers need to review a particular PR, but merging to the base branch is authorized to
restricted members (administrators).

Contributors without an official project role are encouraged to review PRs. This is a
useful signal for maintainers and is a requirement if you are interested in joining the project
in an official capacity.

This repository sits at the base of a large ecosystem: widely deployed, a target for attackers,
and sensitive to performance regressions. Expect a high level of scrutiny on API surface,
security, performance, and maintainability, including on small changes. This reflects the
project's position, not distrust of contributors. Back-and-forth during review is normal.

### Draft PRs

Draft PRs are welcome, especially when exploring new ideas or experimenting with a hypothesis.
However, draft PRs may not receive the same degree of attention, feedback, or scrutiny unless
requested directly. In order to help keep the PR backlog maintainable, drafts older than 6 months
will be closed by the project maintainers. This should not be interpreted as a rejection. Closed
PRs may be reopened by the author when time or interest allows.

### Test coverage

CI reports a code coverage delta on every PR. The threshold is a signal for reviewer attention,
not a merge gate. Above it, coverage is not something reviewers will dwell on. Below it,
reviewers will look more closely at what is uncovered, and the outcome may be adding tests,
agreeing that more coverage is impractical, or restructuring the code for testability. A red
coverage check alone does not block a PR.

### Build failures

Transient CI failures happen on a build matrix of a size this project uses. A red check does not, by itself,
affect how your PR is reviewed.

* Failing across all Java versions and operating systems: likely a real issue in the change,
  worth fixing before requesting review.
* Only one or a handful of jobs failing: probably transient. Most required jobs cannot be
  re-run by contributors, so a maintainer will re-run them, and these failures are not held
  against the contribution.
* If re-runs are slow, a friendly ping on the PR is welcome.

## Project scope

`opentelemetry-java` contains the core components upon which instrumentation and extensions are
built. Its scope is limited to components which implement concepts defined in the
[OpenTelemetry Specification](https://github.com/open-telemetry/opentelemetry-specification). New
features or behavior changes should follow the specification — if the specification doesn't cover
your change, file an issue or submit a PR there first. Exceptions to strict spec alignment include:

* The [API incubator](./api/incubator) and [SDK incubator](./sdk-extensions/incubator) contain
  prototypes discussed in the specification or
  [oteps](https://github.com/open-telemetry/oteps) with a reasonable chance of becoming part of
  the specification, subject to maintainers' discretion.
* Components like the [Kotlin Extension](./extensions/kotlin) are included when required for the
  API / SDK to function in key areas of the Java ecosystem, subject to maintainers' discretion.
* As a general rule, components which implement semantic conventions belong elsewhere.

For an overview of the other repositories in the OpenTelemetry Java ecosystem, see
[opentelemetry.io/docs/languages/java/intro/#repositories](https://opentelemetry.io/docs/languages/java/intro/#repositories).

## Benchmarks

JMH benchmark instructions live in [docs/knowledge/other-tasks.md#benchmarks-jmh](docs/knowledge/other-tasks.md#benchmarks-jmh).
If you are submitting a performance-sensitive PR, include JMH output (number of threads,
iterations, scores with error margins) so reviewers can evaluate the change.

## User-facing documentation

End-user documentation for the Java SDK lives at
[opentelemetry.io/docs/languages/java/](https://opentelemetry.io/docs/languages/java/), with
source in [github.com/open-telemetry/opentelemetry.io](https://github.com/open-telemetry/opentelemetry.io).
If your change affects user-visible behavior — configuration options, new features, changed
defaults — please update or open an issue against the documentation there.
