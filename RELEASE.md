# OpenTelemetry Release Process

This repository uses semantic versioning. Please keep this in mind when choosing version numbers.

1. **Crease a version branch to release from.**

   A version branch must be used to do the release in order to prevent commit race conditions.

1. **Update CHANGELOG.**

   Update CHANGELOG.md with a list changes since the last release. Each entry must include the release number,
   date and a bulleted list of changes where each change is summarized in a single sentence, e.g.
   "- Updated the Jaeger exporter to use the last gRPC version".

1. **Update the version.**

   Update the version to the desired value in `build.gradle`, e.g. `version = "1.2.3"`. Make sure no `SNAPSHOT`
   sufix is appended (that is **only** used during development and when deploying snapshots, as the word implies).

1. **Push a git tag.**

   The tag should be of the format `vN.M.L`, e.g. `git tag v1.2.3; git push origin v1.2.3`. It is recommended
   to have this tag marked in Github along with the CHANGELOG for this version.

1. **Wait for Circle CI.**

   This part is controlled by the Bintray plugin. It publishes the artifacts and syncs to Maven Central.

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
make publish-release-artifacts
```
