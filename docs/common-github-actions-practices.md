# Common GitHub Actions practices

For now this is documenting desired state across the three Java repositories.

Once we agree and implement, will share more broadly across OpenTelemetry
(and surely learn and make more changes).

<details>
<summary>Table of Contents</summary>

<!-- toc -->

- [Have a single required status check for PRs](#have-a-single-required-status-check-for-prs)
- [Configure "cancel-in-progress" on PR workflows](#configure-cancel-in-progress-on-pr-workflows)
- [Prefer `gh` cli over third-party github actions for simple tasks](#prefer-gh-cli-over-third-party-github-actions-for-simple-tasks)
- [Use github action cache to make builds faster and less flaky](#use-github-action-cache-to-make-builds-faster-and-less-flaky)
- [Run CodeQL daily](#run-codeql-daily)
- [Additional checks](#additional-checks)
  - [Automated check for markdown links](#automated-check-for-markdown-links)
  - [Automated check for misspellings](#automated-check-for-misspellings)
  - [Markdown lint](#markdown-lint)
  - [Running checks against changed files only](#running-checks-against-changed-files-only)
- [Configure component owners in contrib repositories](#configure-component-owners-in-contrib-repositories)
- [Release automation](#release-automation)
  - [Workflows that generate PRs](#workflows-that-generate-prs)
  - [Prepare release branch](#prepare-release-branch)
  - [Prepare patch](#prepare-patch)
  - [Generate release notes from change log](#generate-release-notes-from-change-log)
  - [Backporting PRs to a release branch](#backporting-prs-to-a-release-branch)
- [Naming conventions](#naming-conventions)
- [YAML style guide](#yaml-style-guide)

<!-- tocstop -->

</details>

## Have a single required status check for PRs

This avoids needing to modify branch protection required status checks as individual jobs come and go.

```
  required-status-check:
    needs: [ aaa, bbb, ccc, ... ]
    runs-on: ubuntu-latest
    if: always()
    steps:
      - if: |
          needs.aaa.result != 'success' ||
          needs.bbb.result != 'success' ||
          needs.ccc.result != 'success' ||
          ...
        run: exit 1
```

If you have multiple workflows that run on pull requests, there are a couple of options:

* If they have the same `on` triggers, they can be merged into a single workflow.
* Otherwise turn all but one of them into
  [reusable workflows](https://docs.github.com/en/actions/using-workflows/reusing-workflows),
  remove the `pull_request` trigger from the reusable workflows,
  and call the reusable workflows from the single PR workflow.

## Configure "cancel-in-progress" on PR workflows

If the PR build takes some time, and the PR author submits several revisions in a short period of time,
this can end up consuming a lot of GitHub Actions runners.

If your PR workflow only runs on `pull_request`:

```
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number }}
  cancel-in-progress: true
```

If your PR workflow is shared and also runs on CI (i.e. on merge to `main` or release branch):

```
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.sha }}
  cancel-in-progress: true
```

## Prefer `gh` cli over third-party github actions for simple tasks

For example, creating an issue or creating a PR is just as easy using `gh` cli as using a third-party github action.

This preference is because `gh` cli is generally more secure and has less breaking changes.

## Use github action cache to make builds faster and less flaky

This is very build tool specific so no specific tips here on how to implement.

## Run CodeQL daily

```
name: Daily CodeQL analysis

on:
  workflow_dispatch:
  schedule:
    - cron: '30 1 * * *'

jobs:
  analyze:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Initialize CodeQL
        uses: github/codeql-action/init@v1
        with:
          languages: python <-- your language here

      - name: Autobuild
        uses: github/codeql-action/autobuild@v1

      - name: Perform CodeQL Analysis
        uses: github/codeql-action/analyze@v1

  issue:
    # open an issue on failure because it can be easy to miss CI failure notifications
    name: Open issue on failure
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

You may wish to not make this a required check for PRs to avoid unnecessarily blocking of PRs if external links break.

```
  markdown-link-check:
    # release branches are excluded to avoid unnecessary maintenance when external links break
    if: ${{ !startsWith(github.ref_name, 'v') }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install markdown-link-check
        run: npm install -g markdown-link-check

      - name: Run markdown-link-check
        run: |
          find . -type f \
                 -name '*.md' \
                 -not -path './CHANGELOG.md' \
                 -exec markdown-link-check --config .github/scripts/markdown-link-check-config.json {} \;
```

The file `.github/scripts/markdown-link-check-config.json` is for configuring the markdown link check:

```
{
  "retryOn429": true
}
```

`retryOn429` helps with GitHub throttling.

### Automated check for misspellings

https://github.com/client9/misspell only checks against known misspellings,
so while it's not a comprehensive spell checker, it doesn't produce false positives,
and so doesn't get in your way.

You may wish to not make this a required check for PRs to avoid unnecessarily blocking of PRs if
new misspellings are added to the misspell dictionary.

```
  misspell-check:
    # release branches are excluded to avoid unnecessary maintenance when
    # new misspellings are added to the misspell dictionary
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

Go repo uses https://github.com/avto-dev/markdown-lint.

TODO

### Running checks against changed files only

If for some reason some check is running slow, or generates failures on PRs unrelated to changed files,
an option is to run it only against changed files on PRs.

(note, it probably doesn't make sense to do this for link checks, since it's possible for changes in one file
to break a link in an unchanged file)

Here's an example of doing this with the above `misspell-check` workflow:

```
  file-check:
    # release branches are excluded to avoid unnecessary maintenance when
    # new misspellings are added to the misspell dictionary
    if: ${{ !startsWith(github.ref_name, 'v') }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        if: ${{ github.event_name == 'pull_request' }}
        with:
          # target branch is needed in order to perform a diff and check only the changed files
          ref: ${{ github.base_ref }}

      - uses: actions/checkout@v3

      - name: Install misspell
        run: |
          curl -L -o ./install-misspell.sh https://git.io/misspell
          sh ./install-misspell.sh

      - name: Run misspell (diff)
        # only changed files are checked on PRs to avoid unnecessarily blocking PRs when
        # new misspellings are added to the misspell dictionary
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

@dyladan's [component owners action](https://github.com/dyladan/component-owners#component-owners)
works similarly, but does not require granting write access.

### `.github/workflows/component-owners.yml`

```
# assigns reviewers to PRs in a similar way as CODEOWNERS, but doesn't require reviewers
# to have write access to the repository
# see .github/component_owners.yaml for the list of components and their owners
name: Assign component owners

on:
  # pull_request_target is needed instead of just pull_request
  # because repository write permission is needed to assign reviewers
  pull_request_target:

jobs:
  assign-component-owners:
    runs-on: ubuntu-latest
    name: Assign component owners
    steps:
      - uses: dyladan/component-owners@main
```

### `.github/component_owners.yaml`

In the [opentelemetry-java-contrib](https://github.com/open-telemetry/opentelemetry-java-contrib) repository
we have created labels for each component, and have given all component owners triager rights so that they
can assign the labels and triage issues for their component(s).

```
# this file is used by .github/workflows/component-codeowners.yml
#
# component owners must be members of the GitHub OpenTelemetry organization
# so that they can be assigned as reviewers
#
# when updating this file, don't forget to update the component owners sections
# in the associated README.md and the associated `comp:*` labels as needed
components:
  dir1:
    - owner1 <-- github username
    - owner2
```

### `dir1/README.md`

```

...

## Component owners

- [Person One](https://github.com/owner1), Company1
- [Person Two](https://github.com/owner2), Company2

Learn more about component owners in [component-owners.yml].

[component-owners.yml]: https://github.com/open-telemetry/opentelemetry-java-contrib/blob/main/.github/workflows/component-owners.yml
```

## Release automation

### Workflows that generate PRs

Since you can't push directly to `main` or to release branches from workflows due to branch protections,
the next best thing is to generate a PR from the workflow and use a bot which has signed the CLA as commit author.

This is what we use in the OpenTelemetry Java repositories:

```
      - name: Set git user
        run: |
          git config user.name opentelemetry-java-bot
          git config user.email 97938252+opentelemetry-java-bot@users.noreply.github.com
```

### Prepare release branch

The specifics depend a lot on the build tool and your version bumping needs.

For OpenTelemetry Java repositories, we have a workflow which

* Creates the release branch
* Generates a PR against the release branch to bump the version (e.g. from `1.2.0-SNAPSHOT` to `1.2.0`)
* Generates a PR against the `main` branch to bump the version (e.g. from `1.2.0-SNAPSHOT` to `1.3.0-SNAPSHOT`)

See also [workflows that generate PRs](#workflows-that-generate-prs).

### Prepare patch

The specifics depend a lot on the build tool and your version bumping needs.

For OpenTelemetry Java repositories, we have a workflow which generates a PR
against the release branch to bump the version (e.g. from `1.2.0` to `1.2.1`).

See also [workflows that generate PRs](#workflows-that-generate-prs).

### Backporting PRs to a release branch

Having a workflow generate the backport PR is nice because you know that it was a clean cherry-pick
and does not require re-review.

See also [workflows that generate PRs](#workflows-that-generate-prs).

### Release

Create and publish the GitHub release, generating the release notes from change log.

After the release completes, generate a PR against the `main` branch to merge back any change log
updates.

See also [workflows that generate PRs](#workflows-that-generate-prs).

## Naming conventions

Not sure if it's worth sharing these last two sections across all of OpenTelemetry,
but I think worth having this level of consistency across the Java repos.

Use `.yml` extension instead of `.yaml`.

* `.github/workflows/ci.yml` - CI workflow
* `.github/workflows/pr.yml` - PR workflow (if `ci.yml` isn't used for PRs also)
* `.github/workflows/daily-*.yml` - workflows that run once a day
* `.github/workflows/reusable-*.yml` - reusable workflows, unfortunately these cannot be located in subdirectories (yet?)

* `.github/workflows/daily-codeql-analysis.yml`

TODO other common names?

## YAML style guide

TODO
