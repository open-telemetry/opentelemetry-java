# OpenTelemetry Release Process

## Starting the Release

Open the release build workflow in your browser [here](https://github.com/open-telemetry/opentelemetry-java/actions?query=workflow%3A%22Release+Build%22).

You will see a button that says "Run workflow". Press the button, enter the version number you want
to release in the input field that pops up, and then press "Run workflow".

This triggers the release process, which builds the artifacts, updates the README with the new
version numbers, commits the change to the README, publishes the artifacts, and creates and pushes
a git tag with the version number.

## Announcement
   
Once the GitHub workflow completes, go to Github [release
page](https://github.com/open-telemetry/opentelemetry-java/releases), press
`Draft a new release` to write release notes about the new release. If there is already a draft
release notes, just point it at the created tag.

You can use `git log upstream/v$MAJOR.$((MINOR-1)).x..upstream/v$MAJOR.$MINOR.x --graph --first-parent`
or the Github [compare tool](https://github.com/open-telemetry/opentelemetry-java/compare/)
to view a summary of all commits since last release as a reference.

In addition, you can refer to
[CHANGELOG.md](https://github.com/open-telemetry/opentelemetry-java/blob/master/CHANGELOG.md)
for a list of major changes since last release.

## Update release versions in documentations and CHANGELOG files

After releasing is done, you need to update
[CHANGELOG.md](https://github.com/open-telemetry/opentelemetry-java/blob/master/CHANGELOG.md).

Create a PR to mark the new release in
[CHANGELOG.md](https://github.com/census-instrumentation/opencensus-java/blob/master/CHANGELOG.md)
on master branch.

## Patch Release

All patch releases should include only bug-fixes, and must avoid
adding/modifying the public APIs. 

Open the patch release build workflow in your browser [here](https://github.com/open-telemetry/opentelemetry-java/actions?query=workflow%3A%22Patch+Release+Build%22).

You will see a button that says "Run workflow". Press the button, enter the version number you want
to release in the input field for version that pops up and the commits you want to cherrypick for the
patch as a comma-separated list. Then, press "Run workflow".

## Release candidates

Release candidate artifacts are released using the same process described above. The version schema for release candidates
is`v1.2.3-RC$`, where `$` denotes a release candidate version, e.g. `v1.2.3-RC1`.

## Credentials

The following credentials are required for publishing (and automatically set in Circle CI):

* `BINTRAY_USER` and `BINTRAY_KEY`: Bintray username and API Key.
  See [this](https://www.jfrog.com/confluence/display/BT/Bintray+Security#BintraySecurity-APIKeys).

* `SONATYPE_USER` and `SONATYPE_KEY`: Sonatype username and password.

## Releasing from the local setup

Releasing from the local setup can be done providing the previously mentioned four credential values, i.e.
`BINTRAY_KEY`, `BINTRAY_USER`, `SONATYPE_USER` and `SONATYPE_KEY`:

```sh
export BINTRAY_USER=my_bintray_user
export BINTRAY_KEY=my_user_api_key
export SONATYPE_USER=my_maven_user
export SONATYPE_KEY=my_maven_password
export RELEASE_VERSION=2.4.5 # Set version you want to release
./gradlew final -Prelease.version=${RELEASE_VERSION}
```
