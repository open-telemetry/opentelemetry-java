# OpenTelemetry Release Process

## Tagging the Release

The first step in the release process is to create a release branch, bump versions, and create a tag
for the release. Our release branches follow the naming convention of v<major>.<minor>.x, while the
tags include the patch version v<major>.<minor>.<patch>. For example, the same branch v0.3.x would
be used to create all v0.3.* tags (e.g. v0.3.0, v0.3.1).

In this section upstream repository refers to the main opentelemetry-java github repository.

Before any push to the upstream repository you need to create a
[personal access token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line).

Note: The scripts referenced in here may or may not work as-is, depending on your operating system
and command-line shell of choice. In all cases, a description of what needs to be done is also
provided, for clarity.

1.  Create the new version series (eg. `v0.10.x`) branch and push it to GitHub:

    ```bash
    $ MAJOR=0 MINOR=3 PATCH=0 # Set appropriately for new release
    $ VERSION_FILES=(
      build.gradle
      )
    $ git checkout -b v$MAJOR.$MINOR.x master
    $ git push upstream v$MAJOR.$MINOR.x
    ```
    The branch will be automatically protected by the GitHub branch protection rule for release
    branches.

2.  Back on the `master` branch:

    -   Change the version in the root `build.gradle` to the next minor snapshot (e.g.
        `0.11.0-SNAPSHOT`).

    ```bash
    $ git checkout -b bump-version master
    # Change version to next minor (but keep the `-SNAPSHOT` suffix)
    $ sed -i "" 's/[0-9]\+\.[0-9]\+\.[0-9]\+\(.*CURRENT_OPEN_TELEMETRY_VERSION\)/'$MAJOR.$((MINOR+1)).0'\1/' \
      "${VERSION_FILES[@]}"
    $ ./gradlew build
    $ git commit -a -m "Start $MAJOR.$((MINOR+1)).0 development cycle"
    ```

    -   Go through the normal PR review against master, and merge to the master branch on GitHub.
       
3.  From the `vMajor.Minor.x` branch:

    -   Create a new branch called 'release'; change the version in the root build.gradle to remove "-SNAPSHOT" for the next release
        version (eg. `0.10.0`). Commit the result and tag it with the version being released (eg. "v0.10.0"):

    ```bash
    $ git checkout -b release v$MAJOR.$MINOR.x
    # Change the version to remove -SNAPSHOT
    $ sed -i "" 's/-SNAPSHOT\(.*CURRENT_OPEN_TELEMETRY_VERSION\)/\1/' "${VERSION_FILES[@]}"
    $ ./gradlew build
    $ git commit -a -m "Bump version to $MAJOR.$MINOR.$PATCH"
    $ git tag -a v$MAJOR.$MINOR.$PATCH -m "Version $MAJOR.$MINOR.$PATCH"
    ```

    -   Change the version in the root `build.gradle` to the next patch level snapshot version (e.g.
        `0.10.1-SNAPSHOT`). Commit the result:

    ```bash
    # Change version to the next patch level and add -SNAPSHOT
    $ sed -i "" 's/[0-9]\+\.[0-9]\+\.[0-9]\+\(.*CURRENT_OPEN_TELEMETRY_VERSION\)/'$MAJOR.$MINOR.$((PATCH+1))-SNAPSHOT'\1/' \
     "${VERSION_FILES[@]}"
    $ ./gradlew build
    $ git commit -a -m "Bump version to $MAJOR.$MINOR.$((PATCH+1))-SNAPSHOT"
    ```

    -   Create a PR with the current state of your 'release' branch against the `v.M.m.x` branch.
        *Do not merge this PR to the master branch!*
        Go through PR review and after approval, manually push the release tag and updated `v.M.m.x` branch
        to GitHub (note: do not squash the commits when you merge otherwise you
        will lose the release tag!):

    ```bash
    $ git checkout v$MAJOR.$MINOR.x
    $ git merge --ff-only release
    $ git push upstream v$MAJOR.$MINOR.x
    ### this next step, pushing the tag, is what triggers the build that publishes the release:
    $ git push upstream v$MAJOR.$MINOR.$PATCH
    ```

## Announcement
   
Once deployment is done by Circle CI (controlled by the Bintray plugin) , go to Github [release
page](https://github.com/open-telemetry/opentelemetry-java/releases), press
`Draft a new release` to write release notes about the new release.

You can use `git log upstream/v$MAJOR.$((MINOR-1)).x..upstream/v$MAJOR.$MINOR.x --graph --first-parent`
or the Github [compare tool](https://github.com/open-telemetry/opentelemetry-java/compare/)
to view a summary of all commits since last release as a reference.

In addition, you can refer to
[CHANGELOG.md](https://github.com/open-telemetry/opentelemetry-java/blob/master/CHANGELOG.md)
for a list of major changes since last release.

## Update release versions in documentations and CHANGELOG files

After releasing is done, you need to update
[README.md](https://github.com/open-telemetry/opentelemetry-java/blob/master/README.md) and
[CHANGELOG.md](https://github.com/open-telemetry/opentelemetry-java/blob/master/CHANGELOG.md).

Create a PR to mark the new release in
[CHANGELOG.md](https://github.com/census-instrumentation/opencensus-java/blob/master/CHANGELOG.md)
on master branch.

## Patch Release

All patch releases should include only bug-fixes, and must avoid
adding/modifying the public APIs. To cherry-pick one commit use the following
instructions:

- Create and push a tag:

```bash
COMMIT=1224f0a # Set the right commit hash.
git checkout -b cherrypick v$MAJOR.$MINOR.x
git cherry-pick -x $COMMIT
git commit -a -m "Cherry-pick commit $COMMIT"
```

- Go through PR review and merge it to GitHub v$MAJOR.$MINOR.x branch.

- Tag a new patch release when all commits are merged.

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
