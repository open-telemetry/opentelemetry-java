# OpenTelemetry Release Process

Before releasing, it is a good idea to run `./gradlew japicmp` on the main branch and verify that
there are no unexpected public API changes seen in the `docs/apidiffs/current_vs_latest`
directory. Additionally, ensure that appropriate `@since` annotations are added to any additions to
the public APIs.

When preparing the change log, you can use
the [draft-change-log-entries.sh](./.github/scripts/draft-change-log-entries.sh) script to assist
with drafting. Alternatively,
use `git log upstream/v$MAJOR.$((MINOR-1)).x..upstream/v$MAJOR.$MINOR.x --graph --first-parent` or
the Github [compare tool](https://github.com/open-telemetry/opentelemetry-java/compare/) to view a
summary of all commits since last release as a reference.

## Release cadence

This repository roughly targets monthly minor releases from the `main` branch on the Friday after
the first Monday of the month.

## Preparing a new major or minor release

* Close the release milestone if there is one.
* Merge a pull request to `main` updating the `CHANGELOG.md`.
  * The heading for the unreleased entries should be `## Unreleased`.
* Run the [Prepare release branch workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/prepare-release-branch.yml).
  * Press the "Run workflow" button, and leave the default branch `main` selected.
  * Review and merge the two pull requests that it creates
    (one is targeted to the release branch and one is targeted to `main`).

## Preparing a new patch release

All patch releases should include only bug-fixes, and must avoid adding/modifying the public APIs.

In general, patch releases are only made for regressions, security vulnerabilities, memory leaks
and deadlocks.

* Backport pull request(s) to the release branch.
  * Run the [Backport workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/backport.yml).
  * Press the "Run workflow" button, then select the release branch from the dropdown list,
    e.g. `release/v1.9.x`, then enter the pull request number that you want to backport,
    then click the "Run workflow" button below that.
  * Review and merge the backport pull request that it generates.
* Merge a pull request to the release branch updating the `CHANGELOG.md`.
  * The heading for the unreleased entries should be `## Unreleased`.
* Run the [Prepare patch release workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/prepare-patch-release.yml).
  * Press the "Run workflow" button, then select the release branch from the dropdown list,
    e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
  * Review and merge the pull request that it creates for updating the version.

## Making the release

* Run the [Release workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/release.yml).
  * Press the "Run workflow" button, then select the release branch from the dropdown list,
    e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
  * This workflow will publish the artifacts to maven central and will publish a GitHub release
    with release notes based on the change log.
  * Review and merge the pull request that it creates for updating the change log in main
    (note that if this is not a patch release then the change log on main may already be up-to-date,
    in which case no pull request will be created).
  * Review and merge the pull request that it creates for updating the version on
    the [website](https://github.com/open-telemetry/opentelemetry.io), which is created
    via [Reusable - Create website pull request patch](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/reusable-create-website-pull-request.yml).

## Update release versions in documentations

After releasing is done, you need to first update the docs. This needs to happen after artifacts have propagated
to Maven Central so should probably be done an hour or two after the release workflow finishes.

```
./gradlew updateVersionInDocs -Prelease.version=x.y.z
./gradlew japicmp -PapiBaseVersion=a.b.c -PapiNewVersion=x.y.z
./gradlew --refresh-dependencies japicmp
```

Where `x.y.z` is the version just released and `a.b.c` is the previous version.

Create a PR against the main branch with the changes.

## Credentials

The following credentials are required for publishing (and automatically set in Github Actions):

* `GPG_PRIVATE_KEY` and `GPG_PASSWORD`: GPG private key and password for signing.
* `SONATYPE_USER` and `SONATYPE_KEY`: Sonatype username and password.
  * Each maintainer will have their own set of Sonotype credentials with permission to publish to
    the `io.opentelemetry` group prefix.
  * Request [publishing permissions](https://central.sonatype.org/publish/manage-permissions/) by
    commenting on [OSSRH-63768](https://issues.sonatype.org/browse/OSSRH-63768) with confirmation
    from another maintainer.
  * To obtain `SONATYPE_USER` and `SONATYPE_KEY` for your account, login
    to [oss.sonatype.org](https://oss.sonatype.org/) and navigate to Profile -> User Token -> Access
    User Token.

Additionally, credentials are stored with maintainers via
the [OpenTelemetry 1Password](https://opentelemetry.1password.com/signin) account. The following
defines the mapping from Github Action secret keys to 1Password keys:

| Github Actions Key | 1Password Key |
|--------------------|---------------|
| `GPG_PASSWORD` | `opentelemetry-java GPG_PASSWORD` |
| `GPG_PRIVATE_KEY` | `opentelemetry-java GPG_PRIVATE_KEY` |

## Releasing from the local setup

Releasing from the local setup can be done providing the previously mentioned four credential values, i.e.
`GPG_PRIVATE_KEY`, `GPG_PASSWORD`, `SONATYPE_USER` and `SONATYPE_KEY`:

```sh
export SONATYPE_USER=my_maven_user
export SONATYPE_KEY=my_maven_password
export GPG_PRIVATE_KEY=$(cat ~/tmp/gpg.key.txt)
export GPG_PASSWORD=<gpg password>
export RELEASE_VERSION=2.4.5 # Set version you want to release
./gradlew final -Prelease.version=${RELEASE_VERSION}
```
