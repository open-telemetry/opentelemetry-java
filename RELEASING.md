# OpenTelemetry Release Process

Before releasing, it is a good idea to run `./gradlew japicmp` on the main branch and verify that
there are no unexpected public API changes seen in the `docs/apidiffs/current_vs_latest`
directory. Additionally, ensure that appropriate `@since` annotations are added to any additions to
the public APIs.

When preparing the change log, you can use
`git log upstream/v$MAJOR.$((MINOR-1)).x..upstream/v$MAJOR.$MINOR.x --graph --first-parent`
or the Github [compare tool](https://github.com/open-telemetry/opentelemetry-java/compare/)
to view a summary of all commits since last release as a reference.

## Preparing a new major or minor release

* Close the release milestone if there is one.
* Merge a pull request to `main` updating the `CHANGELOG.md`.
  * The heading for the release should include the release version but not the release date, e.g.
    `## Version 1.9.0 (Unreleased)`.
* Run the [Prepare release branch workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/prepare-release-branch.yml).
* Review and merge the two pull requests that it creates
  (one is targeted to the release branch and one is targeted to the `main` branch).

## Preparing a new patch release

All patch releases should include only bug-fixes, and must avoid adding/modifying the public APIs.

In general, patch releases are only made for regressions, memory leaks and deadlocks.

* Backport pull request(s) to the release branch.
  * Run the [Backport workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/backport.yml).
  * Press the "Run workflow" button, then select the release branch from the dropdown list,
    e.g. `release/v1.9.x`, then enter the pull request number that you want to backport,
    then click the "Run workflow" button below that.
  * Review and merge the backport pull request that it generates.
* Merge a pull request to the release branch updating the `CHANGELOG.md`.
  * The heading for the release should include the release version but not the release date, e.g.
    `## Version 1.9.1 (Unreleased)`.
* Run the [Prepare patch release workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/prepare-patch-release.yml).
  * Press the "Run workflow" button, then select the release branch from the dropdown list,
    e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
* Review and merge the pull request that it creates.

## Making the release

Run the [Release workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/release.yml).

* Press the "Run workflow" button, then select the release branch from the dropdown list,
  e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
* This workflow will publish the artifacts to maven central and will publish a GitHub release
  with release notes based on the change log.
* Review and merge the pull request that the release workflow creates against the release branch
  which adds the release date to the change log.

## After the release

Run the [Merge change log to main workflow](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/merge-change-log-to-main.yml).

* Press the "Run workflow" button, then select the release branch from the dropdown list,
  e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
* This will create a pull request that merges the change log updates from the release branch
  back to main.
* Review and merge the pull request that it creates.
* This workflow will fail if there have been conflicting change log updates introduced in main,
  in which case you will need to merge the change log updates manually and send your own pull
  request against main.

## Update release versions in documentations

After releasing is done, you need to first update the docs. This needs to happen after artifacts have propagated
to Maven Central so should probably be done an hour or two after the release workflow finishes.

```
./gradlew updateVersionInDocs -Prelease.version=x.y.z
./gradlew japicmp -PapiBaseVersion=a.b.c -PapiNewVersion=x.y.z
./gradlew --refresh-dependencies japicmp
```

Where `x.y.z` is the version just released and `a.b.c` is the previous version.

Create a PR to mark the new release in README.md on the main branch.

Finally, update the [website docs][] to refer to the newly released version.

[website docs]: https://github.com/open-telemetry/opentelemetry.io/tree/main/content/en/docs/instrumentation/java

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
