# Repository settings

Repository settings in addition to what's documented already at
<https://github.com/open-telemetry/community/blob/main/docs/how-to-configure-new-repository.md>.

## General > Pull Requests

- Allow squash merging > Default to pull request title

- Allow auto-merge

## Actions > General

- Fork pull request workflows from outside collaborators:
  "Require approval for first-time contributors who are new to GitHub"

  (To reduce friction for new contributors,
  as the default is "Require approval for first-time contributors")

- Workflow permissions
  - Default permissions granted to the `GITHUB_TOKEN` when running workflows in this repository:
    Read repository contents and packages permissions
  - Allow GitHub Actions to create and approve pull requests: UNCHECKED

## Rules > Rulesets

### `main` and release branches

- Targeted branches:
  - `main`
  - `release/*`
- Branch rules
  - Restrict deletions: CHECKED
  - Require linear history: CHECKED
  - Require a pull request before merging: CHECKED
    - Required approvals: 1
    - Require review from Code Owners: CHECKED
    - Allowed merge methods: Squash
  - Require status checks to pass
    - Do not require status checks on creation: CHECKED
    - Status checks that are required
      - EasyCLA
      - `required-status-check`
      - `gradle-wrapper-validation`
  - Block force pushes: CHECKED
  - Require code scanning results: CHECKED
    - CodeQL
      - Security alerts: High or higher
      - Alerts: Errors

### `benchmarks` branch

- Targeted branches:
  - `benchmarks`
- Branch rules
  - Restrict deletions: CHECKED
  - Require linear history: CHECKED
  - Block force pushes: CHECKED

### Old-style release branches

- Targeted branches:
  - `v0.*`
  - `v1.*`
- Branch rules
  - Restrict creations: CHECKED
  - Restrict updates: CHECKED
  - Restrict deletions: CHECKED

### Restrict branch creation

- Targeted branches
  - Exclude:
    - `release/*`
    - `renovate/**/*`
    - `otelbot/**/*`
    - `revert-*/**/*` (these are created when using the GitHub UI to revert a PR)
- Restrict creations: CHECKED

### Restrict updating tags

- Targeted tags
  - All tags
- Restrict updates: CHECKED
- Restrict deletions: CHECKED

## Branch protections

### `main`, `release/*`

- Restrict who can push to matching branches: CHECKED

## Code security and analysis

- Secret scanning: Enabled

## Secrets and variables > Actions

- `GPG_PASSWORD` - stored in OpenTelemetry-Java 1Password
- `GPG_PRIVATE_KEY` - stored in OpenTelemetry-Java 1Password
- `SONATYPE_KEY` - owned by [@jack-berg](https://github.com/jack-berg)
- `SONATYPE_USER` - owned by [@jack-berg](https://github.com/jack-berg)
