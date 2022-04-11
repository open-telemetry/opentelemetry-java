# Common GitHub Actions practices

For now this is documenting the desired state across the three Java repositories.

Once we agree and implement, will share more broadly across OpenTelemetry
(and surely learn and make more changes).

<details>
<summary>Table of Contents</summary>

<!-- toc -->

- [Have a single required status check for pull requests](#have-a-single-required-status-check-for-pull-requests)
- [Configure "cancel-in-progress" on pull request workflows](#configure-cancel-in-progress-on-pull-request-workflows)
- [Prefer `gh` cli over third-party GitHub actions for simple tasks](#prefer-gh-cli-over-third-party-github-actions-for-simple-tasks)
- [Use GitHub action cache to make builds faster and less flaky](#use-github-action-cache-to-make-builds-faster-and-less-flaky)
- [Run CodeQL daily](#run-codeql-daily)
- [Additional checks](#additional-checks)
  - [Automated check for markdown links](#automated-check-for-markdown-links)
  - [Automated check for misspellings](#automated-check-for-misspellings)
  - [Markdown lint](#markdown-lint)
  - [Running checks against changed files only](#running-checks-against-changed-files-only)
- [Configure component owners in contrib repositories](#configure-component-owners-in-contrib-repositories)
- [Release automation](#release-automation)
  - [Workflows that generate pull requests](#workflows-that-generate-pull-requests)
  - [Prepare release branch](#prepare-release-branch)
  - [Prepare patch](#prepare-patch)
  - [Backport pull requests to a release branch](#backport-pull-requests-to-a-release-branch)
  - [Release](#release)
  - [Update the change log with the release date](#update-the-change-log-with-the-release-date)
  - [Send a pull request to another repository](#send-a-pull-request-to-another-repository)
  - [Merge change log updates back to main](#merge-change-log-updates-back-to-main)
- [Workflow file naming conventions](#workflow-file-naming-conventions)
- [Workflow YAML style guide](#workflow-yaml-style-guide)

<!-- tocstop -->

</details>

## Have a single required status check for pull requests

This avoids needing to modify branch protection required status checks as individual jobs
(and job matrix items) come and go.

```yaml
  required-status-check:
    needs:
      - aaa
      - bbb
      - ccc
    runs-on: ubuntu-latest
    if: always()
    steps:
      - if: |
          needs.aaa.result != 'success' ||
          needs.bbb.result != 'success' ||
          needs.ccc.result != 'success'
        run: exit 1
```

If you have multiple workflows that run on pull requests, there are a couple of options:

* If they have the same `on` triggers, they can be merged into a single workflow.
* Otherwise turn them into
  [reusable workflows](https://docs.github.com/en/actions/using-workflows/reusing-workflows),
  and call them from a single workflow.

## Configure "cancel-in-progress" on pull request workflows

If the pull request build takes some time, and the author submits several revisions in a short
period of time, this can end up consuming a lot of GitHub Actions runners.

If your pull request workflow only runs on `pull_request`:

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number }}
  cancel-in-progress: true
```

If your pull request workflow is shared and also runs on CI (i.e. on merge to `main` or release branch):

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.sha }}
  cancel-in-progress: true
```

## Prefer `gh` cli over third-party GitHub actions for simple tasks

For example, creating an issue or creating a pull request is just as easy using `gh` cli as using a third-party GitHub action.

This preference is because `gh` cli is generally more secure and has less breaking changes
compared to third-party GitHub actions.

## Use GitHub action cache to make builds faster and less flaky

This is very build tool specific so no specific tips here on how to implement.

## Run CodeQL daily

```yaml
name: CodeQL (daily)

on:
  schedule:
    - cron: '30 1 * * *'
  workflow_dispatch:

jobs:
  analyze:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Initialize CodeQL
        uses: github/codeql-action/init@v1
        with:
          languages: python  <-- your language here

      - name: Autobuild
        uses: github/codeql-action/autobuild@v1

      - name: Perform CodeQL analysis
        uses: github/codeql-action/analyze@v1

  open-issue-on-failure:
    # open an issue on failure because it can be easy to miss CI failure notifications
    needs: analyze
    if: failure()
    steps:
      - uses: actions/checkout@v3

      - name: Create issue
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          gh issue create --title "$GITHUB_WORKFLOW #$GITHUB_RUN_NUMBER failed" \
                          --label bug \
                          --body "See [$GITHUB_WORKFLOW #$GITHUB_RUN_NUMBER](https://github.com/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID)."
```

## Additional checks

### Automated check for markdown links

https://github.com/tcort/markdown-link-check checks markdown files for valid links and anchors.

It is recommended to not make this a required check for pull requests to avoid blocking pull
requests if external links break.

```yaml
  # this is not a required check to avoid blocking pull requests if external links break
  markdown-link-check:
    # release branches are excluded to avoid unnecessary maintenance if external links break
    if: ${{ !startsWith(github.ref_name, 'v') }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install markdown-link-check
        run: npm install -g markdown-link-check

      - name: Run markdown-link-check
        run: |
          # --quiet displays errors only, making them easier to find in the log
          find . -type f \
                 -name '*.md' \
                 -not -path './CHANGELOG.md' \
                 | xargs markdown-link-check \
                         --config .github/scripts/markdown-link-check-config.json \
                         --quiet
```

The file `.github/scripts/markdown-link-check-config.json` is for configuring the markdown link check:

```json
{
  "retryOn429": true
}
```

`retryOn429` helps with GitHub throttling.

### Automated check for misspellings

https://github.com/client9/misspell only checks against known misspellings,
so while it's not a comprehensive spell checker, it doesn't produce false positives,
and so doesn't get in your way.

It is recommended to not make this a required check for pull requests to avoid blocking pull
requests if new misspellings are added to the misspell dictionary.

```yaml
  # this is not a required check to avoid blocking pull requests if new misspellings are added
  # to the misspell dictionary
  misspell-check:
    # release branches are excluded to avoid unnecessary maintenance if new misspellings are
    # added to the misspell dictionary
    if: ${{ !startsWith(github.ref_name, 'v') }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install misspell
        run: |
          curl -L -o ./install-misspell.sh https://git.io/misspell
          sh ./install-misspell.sh

      - name: Run misspell
        run: bin/misspell -error .
```

### Markdown lint

Specification repo uses https://github.com/DavidAnson/markdownlint.

Go, JavaScript repos use https://github.com/avto-dev/markdown-lint github action.

C++ uses markdownlint-cli (which is same that is used by avto-dev/markdown-lint github action).

TODO

### Running checks against changed files only

If for some reason some check is running slow, or generates failures on pull requests unrelated to changed files,
an option is to run it only against changed files on pull requests.

(note, it probably doesn't make sense to do this for link checks, since it's possible for changes in one file
to break a link in an unchanged file)

Here's an example of doing this with the above `misspell-check` workflow:

```yaml
  misspell-check:
    # release branches are excluded to avoid unnecessary maintenance if new misspellings are
    # added to the misspell dictionary
    if: ${{ !startsWith(github.ref_name, 'v') }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        if: ${{ github.event_name == 'pull_request' }}
        with:
          # target branch is needed to perform a diff and check only the changed files
          ref: ${{ github.base_ref }}

      - uses: actions/checkout@v3

      - name: Install misspell
        run: |
          curl -L -o ./install-misspell.sh https://git.io/misspell
          sh ./install-misspell.sh

      - name: Run misspell (diff)
        if: ${{ github.event_name == 'pull_request' }}
        run: |
          git diff --name-only --diff-filter=ACMRTUXB origin/$GITHUB_BASE_REF \
            | xargs bin/misspell -error

      - name: Run misspell (full)
        if: ${{ github.event_name != 'pull_request' }}
        run: bin/misspell -error .
```

## Configure component owners in contrib repositories

Using CODEOWNERS to assign reviewers requires all reviewers to have write access to the repository,
which brings along a lot of [additional permissions][].

[additional permissions]: https://docs.github.com/en/organizations/managing-access-to-your-organizations-repositories/repository-roles-for-an-organization#permissions-for-each-role

The [component owners action](https://github.com/dyladan/component-owners#component-owners)
works similarly, but does not require granting write access.

### `.github/workflows/assign-reviewers.yml`

```yaml
# assigns reviewers to pull requests in a similar way as CODEOWNERS, but doesn't require
# reviewers to have write access to the repository
# see .github/component_owners.yaml for the list of components and their owners
name: Assign reviewers

on:
  # pull_request_target is needed instead of just pull_request
  # because repository write permission is needed to assign reviewers
  pull_request_target:

jobs:
  assign-reviewers:
    runs-on: ubuntu-latest
    steps:
      - uses: dyladan/component-owners@main
```

### `.github/component_owners.yaml`

In the [opentelemetry-java-contrib](https://github.com/open-telemetry/opentelemetry-java-contrib)
repository we have created labels for each component, and have given all component owners triager
rights so that they can assign labels and triage issues for their component(s).

```yaml
# this file is used by .github/workflows/assign-reviewers.yml to assign component owners as
# reviewers to pull requests that touch files in their component(s)
#
# component owners must be members of the GitHub OpenTelemetry organization
# so that they can be assigned as reviewers
#
# when updating this file, don't forget to update the component owners sections
# in the associated README.md and update the associated `comp:*` labels if needed
components:
  dir1:
    - owner1  <-- GitHub username
    - owner2
```

### `dir1/README.md`

```markdown

...

## Component owners

- [Person One](https://github.com/owner1), Company1
- [Person Two](https://github.com/owner2), Company2

Learn more about component owners in [component_owners.yml].

[component_owners.yml]: ../.github/component_owners.yml
```

## Release automation

Here's some sample `RELEASING.md` documentation that goes with the automation below.

```markdown
## Preparing a new major or minor release

* Close the release milestone if there is one.
* Merge a pull request to `main` updating the `CHANGELOG.md`.
  * The heading for the release should include the release version but not the release date, e.g.
  `## Version 1.9.0 (unreleased)`.
* Run the [Prepare release branch workflow](.github/workflows/prepare-release-branch.yml).
* Review and merge the two pull requests that it creates
  (one is targeted to the release branch and one is targeted to `main`).

## Preparing a new patch release

* Backport pull request(s) to the release branch.
  * Run the [Backport workflow](.github/workflows/backport.yml).
  * Press the "Run workflow" button, then select the release branch from the dropdown list,
    e.g. `release/v1.9.x`, then enter the pull request number that you want to backport,
    then click the "Run workflow" button below that.
  * Review and merge the backport pull request that it generates.
* Merge a pull request to the release branch updating the `CHANGELOG.md`.
  * The heading for the release should include the release version but not the release date, e.g.
  `## Version 1.9.0 (unreleased)`.
* Run the [Prepare patch release workflow](.github/workflows/prepare-patch-release.yml).
  * Press the "Run workflow" button, then select the release branch from the dropdown list,
    e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
* Review and merge the pull request that it creates.

## Making the release

Run the [Release workflow](.github/workflows/release.yml).

* Press the "Run workflow" button, then select the release branch from the dropdown list,
  e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
* This workflow will publish the artifacts to maven central and will publish a GitHub release
  with release notes based on the change log.
* Lastly, if there were any change log updates in the release branch that need to be merged back
  to `main`, the workflow will create a pull request if the updates can be cleanly applied,
  or it will fail this last step if the updates cannot be cleanly applied.

## After the release

Run the [Merge change log to main workflow](.github/workflows/merge-change-log-to-main.yml).

* Press the "Run workflow" button, then select the release branch from the dropdown list,
  e.g. `release/v1.9.x`, and click the "Run workflow" button below that.
* This will create a pull request that merges the change log updates from the release branch
  back to `main`.
* Review and merge the pull request that it creates.
* This workflow will fail if there have been conflicting change log updates introduced in `main`,
  in which case you will need to merge the change log updates manually and send your own pull
  request against `main`.
```

### Workflows that generate pull requests

Since you can't push directly to `main` or to release branches from workflows (due to branch protections),
the next best thing is to generate a pull request from the workflow and use a bot which has signed the CLA as commit author.

This is what we use in the OpenTelemetry Java repositories:

```yaml
      - name: Set git user
        run: |
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com
```

### Prepare release branch

Uses release branch naming convention `release/v*`.

The specifics below depend a lot on your specific version bumping needs.

For OpenTelemetry Java repositories, the version in `main` always ends with `-SNAPSHOT`,
so preparing the release branch involves

* removing `-SNAPSHOT` from the version on the release branch
  (e.g. updating the version from `1.2.0-SNAPSHOT` to `1.2.0`)
* bumping the version to the next `-SNAPSHOT` on `main`
  (e.g. updating the version from `1.2.0-SNAPSHOT` to `1.3.0-SNAPSHOT`)

```yaml
name: Prepare release branch
on:
  workflow_dispatch:

jobs:
  create-pull-request-against-release-branch:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Create release branch
        id: create-release-branch
        run: |
          version=$(...)  <-- get the version that is planned to be released
          release_branch_name=$(echo $version | sed -E 's,([0-9]+)\.([0-9]+)\.0,release/v\1.\2.x,')

          git push origin HEAD:$release_branch_name

          echo "VERSION=$version" >> $GITHUB_ENV
          echo "RELEASE_BRANCH_NAME=$release_branch_name" >> $GITHUB_ENV

      - name: Bump version
        run: |
          .github/scripts/update-versions.sh $VERSION-SNAPSHOT $VERSION

      - name: Set up git name
        run: |
          # TODO replace opentelemetry-java-bot info with your bot account
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com

      - name: Create pull request against release branch
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          msg="Prepare release $VERSION"
          git commit -a -m "$msg"
          git push origin HEAD:prepare-release-$VERSION
          gh pr create --title "[$RELEASE_BRANCH_NAME] $msg" \
                       --body "$msg" \
                       --head prepare-release-$VERSION \
                       --base $RELEASE_BRANCH_NAME

  create-pull-request-against-main:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Bump version on main
        run: |
          version=$(...)  <-- get the minor version that is planning to be released
          if [[ $version =~ ([0-9]+).([0-9]+).0 ]]; then
            major="${BASH_REMATCH[1]}"
            minor="${BASH_REMATCH[2]}"
          else
            echo "unexpected version: $version"
            exit 1
          fi
          next_version="$major.$((minor + 1)).0"
          .github/scripts/update-versions.sh $version-SNAPSHOT $next_version-SNAPSHOT

      - name: Set up git name
        run: |
          # TODO replace opentelemetry-java-bot info with your bot account
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com

      - name: Create pull request against main
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          msg="Bump version"
          git commit -a -m "$msg"
          git push origin HEAD:bump-snapshot-version
          gh pr create --title "$msg" \
                       --body "$msg" \
                       --head bump-snapshot-version \
                       --base main
```

### Prepare patch

The specifics depend a lot on the build tool and your version bumping needs.

For OpenTelemetry Java repositories, we have a workflow which generates a pull request
against the release branch to bump the version (e.g. from `1.2.0` to `1.2.1`).

```yaml
name: Prepare patch release
on:
  workflow_dispatch:

jobs:
  prepare-patch-release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set environment variables
        run: |
          prior_version=$(...)  <-- get the prior release version
          if [[ $prior_version =~ ([0-9]+.[0-9]+).([0-9]+) ]]; then
            major_minor="${BASH_REMATCH[1]}"
            patch="${BASH_REMATCH[2]}"
          else
            echo "unexpected version: $prior_version"
            exit 1
          fi
          echo "VERSION=$major_minor.$((patch + 1))" >> $GITHUB_ENV
          echo "PRIOR_VERSION=$prior_version" >> $GITHUB_ENV

      - name: Bump version
        run: |
          .github/scripts/update-versions.sh $PRIOR_VERSION $VERSION

      - name: Set up git name
        run: |
          # TODO replace opentelemetry-java-bot info with your bot account
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com

      - name: Create pull request
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          msg="Prepare release $VERSION"
          git commit -a -m "$msg"
          git push origin HEAD:prepare-release-$VERSION
          gh pr create --title "[$GITHUB_REF_NAME] $msg" \
                       --body "$msg" \
                       --head prepare-release-$VERSION \
                       --base $GITHUB_REF_NAME
```

### Backport pull requests to a release branch

Having a workflow generate backport pull requests is nice because then you know that it was a clean
cherry-pick and that it does not require re-review.

```yaml
name: Backport a pull request
on:
  workflow_dispatch:
    inputs:
      number:
        description: "The pull request # to backport"
        required: true

jobs:
  backport:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          # history is needed to run git cherry-pick below
          fetch-depth: 0

      - name: Set up git name
        run: |
          # TODO replace opentelemetry-java-bot info with your bot account
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com

      - name: Create pull request
        env:
          NUMBER: ${{ github.event.inputs.number }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          commit=$(gh pr view $NUMBER --json mergeCommit --jq .mergeCommit.oid)
          title=$(gh pr view $NUMBER --json title --jq .title)
          url=$(gh pr view $NUMBER --json url --jq .url)

          git cherry-pick $commit
          git push origin HEAD:backport-$NUMBER-to-$GITHUB_REF_NAME

          gh pr create --title "[$GITHUB_REF_NAME] $title" \
                       --body "Clean cherry-pick of #$NUMBER to the $GITHUB_REF_NAME branch." \
                       --head backport-$NUMBER-to-$GITHUB_REF_NAME \
                       --base $GITHUB_REF_NAME
```

### Release

#### Autogenerating the release notes

```yaml
      - name: Set environment variables
        run: |
          version=$(...)  <-- get the current version (the one that is being released)
          if [[ $version =~ ([0-9]+).([0-9]+).([0-9]+) ]]; then
            major="${BASH_REMATCH[1]}"
            minor="${BASH_REMATCH[2]}"
            patch="${BASH_REMATCH[3]}"
          else
            echo "unexpected version: $version"
            exit 1
          fi
          if [[ $patch == 0 ]]; then
            if [[ $minor == 0 ]]; then
              prior_major=$((major - 1))
              prior_minor=$(grep -Po "^## Version $prior_major.\K([0-9]+)" CHANGELOG.md  | head -1)
              prior_version="$prior_major.$prior_minor"
            else
              prior_version="$major.$((minor - 1)).0"
            fi
          else
              prior_version="$major.$minor.$((patch - 1))"
          fi
          echo "VERSION=$version" >> $GITHUB_ENV
          echo "PRIOR_VERSION=$prior_version" >> $GITHUB_ENV

      - name: Generate release notes
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          # conditional block not indented because of the heredoc
          if [[ $VERSION != *.0 ]]; then
          cat > release-notes.txt << EOF
          This is a patch release on the previous $PRIOR_VERSION release, fixing the issue(s) below.

          EOF
          fi

          # TODO this is dependent on the conventions you follow in your CHANGELOG.md
          sed -n "/^## Version $VERSION/,/^## Version /p" CHANGELOG.md \
            | tail -n +2 \
            | head -n -1 \
            | perl -0pe 's/^\n+//g' \
            | perl -0pe 's/\n+$/\n/g' \
            | sed -r "s,\[#([0-9]+)]\(https://github.com/$GITHUB_REPOSITORY/(pull|issues)/[0-9]+\),#\1," \
            | perl -0pe 's/\n +/ /g' \
            >> release-notes.txt
```

#### Create the GitHub release

Add `--draft` to the `gh release create` command if you want to review the release before hitting
the "Publish release" button yourself.

You will need to remove `--discussion-category announcements` if you add `--draft`
(you can still choose whether to select "Create a discussion for this release" before
hitting the "Publish release" button).

```yaml
      - name: Create GitHub release
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          gh release create --target $GITHUB_REF_NAME \
                            --title "Version $VERSION" \
                            --notes-file release-notes.txt \
                            --discussion-category announcements \
                            v$VERSION
```

#### Update the change log with the release date

```yaml
      - name: Update the change log with the release date
        run: |
          date=$(gh release view v$VERSION --json publishedAt --jq .publishedAt | sed 's/T.*//')
          sed -ri "s/## Version $VERSION .*/## Version $VERSION ($date)/" CHANGELOG.md

      - name: Set git user
        run: |
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com

      - name: Create pull request against the release branch
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          msg="Add $VERSION release date to the change log"
          git commit -a -m "$msg"
          git push origin HEAD:add-$VERSION-release-date
          gh pr create --title "[$GITHUB_REF_NAME] $msg" \
                       --body "$msg" \
                       --head add-$VERSION-release-date \
                       --base $GITHUB_REF_NAME
```

#### Send a pull request to another repository

For example to send a PR to notify/update another repository that a new release is available
as part of the release workflow.

```yaml
      - uses: actions/checkout@v3
        with:
          repository: opentelemetry-java-bot/opentelemetry-operator
          # this is the PAT used for "git push" below
          token: ${{ secrets.OPENTELEMETRY_JAVA_BOT_TOKEN }}

      - name: Initialize pull request branch
        run: |
          git remote add upstream https://github.com/open-telemetry/opentelemetry-operator.git
          git fetch upstream
          git checkout -b update-opentelemetry-javaagent-to-$VERSION upstream/main

      - name: Bump version
        run: |
          echo $VERSION > autoinstrumentation/java/version.txt

      - name: Set git user
        run: |
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com

      - name: Create pull request against opentelemetry-operator
        env:
          # this is the PAT used for "gh pr create" below
          GITHUB_TOKEN: ${{ secrets.OPENTELEMETRY_JAVA_BOT_TOKEN }}
        run: |
          msg="Update opentelemetry-javaagent version to $VERSION"
          git commit -a -m "$msg"

          # gh pr create doesn't have a way to explicitly specify different head and base
          # repositories currently, but it will implicitly pick up the head from a different
          # repository if you set up a tracking branch

          git push --set-upstream origin update-opentelemetry-javaagent-to-$VERSION

          gh pr create --title "$msg" \
                       --body "$msg" \
                       --repo open-telemetry/opentelemetry-operator
                       --base main
```

#### Merge change log updates back to `main`

This needs to be a separate workflow from the release workflow, because you will need to merge the
pull request that the release workflow creates to add the release date to the change log first
before running this workflow.

Note that this workflow will fail if there have been conflicting change log updates introduced in
`main`, in which case you will need to merge the change log updates manually and send your own pull
request against `main`.

```yaml
name: Merge change log to main
on:
  workflow_dispatch:

jobs:
  create-pull-request:
    runs-on: ubuntu-20.04
    steps:
      - uses: actions/checkout@v3
        with:
          # this workflow is run against the release branch (see usage of GITHUB_REF_NAME below)
          # but it is creating a pull request against main
          ref: main
          # history is needed to run format-patch below
          fetch-depth: 0

      - name: Set git user
        run: |
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com

        # this will fail if there have been conflicting change log updates introduced in main
      - name: Create pull request against main
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          git format-patch --stdout HEAD..origin/$GITHUB_REF_NAME CHANGELOG.md | git apply
          msg="Merge change log updates from $GITHUB_REF_NAME to main"
          git commit -a -m "$msg"
          git push origin HEAD:merge-change-log-updates-to-main
          gh pr create --title "$msg" \
                       --body "$msg" \
                       --head merge-change-log-updates-to-main \
                       --base main
```

## Workflow file naming conventions

Not sure if it's worth sharing these last two sections across all of OpenTelemetry,
but I think it's worth having this level of consistency across the Java repos.

Use `.yml` extension instead of `.yaml`.

* `.github/workflows/build.yml` - primary build workflow (CI)
* `.github/workflows/build-pull-request.yml` - pull request workflow (if `build.yml` isn't used also for pull requests)
* `.github/workflows/build-daily.yml` - if you have a daily build in addition to normal CI builds
* `.github/workflows/reusable-*.yml` - reusable workflows, unfortunately these cannot be located in subdirectories (yet?)
* `.github/workflows/backport.yml`
* `.github/workflows/codeql-daily.yml`

## Workflow YAML style guide

Workflow names - [Sentence case](https://en.wikipedia.org/wiki/Letter_case#Sentence_case)

Job names - [kebab-case](https://en.wikipedia.org/wiki/Letter_case#Kebab_case)

Step names - [Sentence case](https://en.wikipedia.org/wiki/Letter_case#Sentence_case)
