# OpenTelemetry Release Process

## Starting the Release

Before releasing, it is a good idea to run `./gradlew japicmp` on the main branch
and verify that there are no unexpected public API changes seen in the `docs/apidiffs/current_vs_latest`
directory.

Open the release build workflow in your browser [here](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/release-build.yml).

You will see a button that says "Run workflow". Press the button, enter the version number you want
to release in the input field that pops up, and then press "Run workflow".

This triggers the release process, which builds the artifacts. It will not automatically update the 
documentation, because the Github Actions cannot push changes to the main branch.

## Announcement
   
Once the GitHub workflow completes, go to Github [release
page](https://github.com/open-telemetry/opentelemetry-java/releases), press
`Draft a new release` to write release notes about the new release. If there is already a draft
release notes, just point it at the created tag.

You can use `git log upstream/v$MAJOR.$((MINOR-1)).x..upstream/v$MAJOR.$MINOR.x --graph --first-parent`
or the Github [compare tool](https://github.com/open-telemetry/opentelemetry-java/compare/)
to view a summary of all commits since last release as a reference.

In addition, you can refer to
[CHANGELOG.md](https://github.com/open-telemetry/opentelemetry-java/blob/main/CHANGELOG.md)
for a list of major changes since last release.

## Update release versions in documentations and CHANGELOG files

After releasing is done, you need to first update the docs.

```
./gradlew updateVersionInDocs -Prelease.version=x.y.z
./gradlew japicmp -PapiBaseVersion=a.b.c -PapiNewVersion=x.y.z
./gradlew --refresh-dependencies japicmp
```

Where `x.y.z` is the version just released and `a.b.c` is the previous version.

Next, update the
[CHANGELOG.md](https://github.com/open-telemetry/opentelemetry-java/blob/main/CHANGELOG.md).

Create a PR to mark the new release in README.md and CHANGELOG.md on the main branch.

Finally, update the files `website_docs` directory to point at the newly released version. Once that has
been merged to the main branch, use the "Update OpenTelemetry Website" github action to create a PR 
in the website repository with the changes.

## Patch Release

All patch releases should include only bug-fixes, and must avoid
adding/modifying the public APIs. 

Open the patch release build workflow in your browser [here](https://github.com/open-telemetry/opentelemetry-java/actions/workflows/patch-release-build.yml).

You will see a button that says "Run workflow". Press the button, enter the version number you want
to release in the input field for version that pops up and the commits you want to cherrypick for the
patch as a comma-separated list. Then, press "Run workflow".

If the commits cannot be cleanly applied to the release branch, for example because it has diverged
too much from main, then the workflow will fail before building. In this case, you will need to
prepare the release branch manually.

This example will assume patching into release branch `v1.2.x` from a git repository with remotes
named `origin` and `upstream`.

```
$ git remote -v
origin	git@github.com:username/opentelemetry-java.git (fetch)
origin	git@github.com:username/opentelemetry-java.git (push)
upstream	git@github.com:open-telemetry/opentelemetry-java.git (fetch)
upstream	git@github.com:open-telemetry/opentelemetry-java.git (push)
```

First, checkout the release branch

```
git fetch upstream v1.2.x
git checkout upstream/v1.2.x
```

Apply cherrypicks manually and commit. It is ok to apply multiple cherrypicks in a single commit.
Use a commit message such as "Manual cherrypick for commits commithash1, commithash2".

After commiting the change, push to your fork's branch.

```
git push origin v1.2.x
```

Create a PR to have code review and merge this into upstream's release branch. As this was not
applied automatically, we need to do code review to make sure the manual cherrypick is correct.

After it is merged, Run the patch release workflow again, but leave the commits input field blank.
The release will be made with the current state of the release branch, which is what you prepared
above.

## Credentials

The following credentials are required for publishing (and automatically set in Circle CI):

* `GPG_PRIVATE_KEY` and `GPG_PASSWORD`: GPG private key and password for signing
  - Note, currently only @anuraaga has this and we need to find a way to safely share secrets in the
    OpenTelemetry project, for example with a password manager. In the worst case if you need to
    release manually and cannot get a hold of it, you can generate a new key but don't forget to
    upload the public key to keyservers.

* `SONATYPE_USER` and `SONATYPE_KEY`: Sonatype username and password.

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
