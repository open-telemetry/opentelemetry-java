# Knowledge Index

Repository guidance for coding and review, written to be useful for both humans and machines.

**For humans**: these documents use plain language and examples rather than rigid rules. Where
something is a strong convention, it's described as such. Where something is a matter of
judgment, it's described that way too.

**For machines**: load only files relevant to the current scope — the "Load when" column below
is the signal.

## Topics

| File                                           | Load when                                                                                                               |
|------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| [build.md](build.md)                           | Always — build requirements and common tasks                                                                            |
| [general-patterns.md](general-patterns.md)     | Always — style, nullability, visibility, AutoValue, locking, logging, internal & impl packages                          |
| [api-design.md](api-design.md)                 | Public API additions, removals, renames, or deprecations or implementations; null guards; stable vs alpha compatibility |
| [gradle-conventions.md](gradle-conventions.md) | `build.gradle.kts` or `settings.gradle.kts` changes; new modules                                                        |
| [testing-patterns.md](testing-patterns.md)     | Test files in scope — assertions, test utilities, test suites                                                           |
| [other-tasks.md](other-tasks.md)               | Dev environment setup, benchmarks, composite builds, native image tests, OTLP protobuf updates                          |

## Conventions

- File names are kebab-cased and topic-oriented. Most follow a `<domain>-<focus>.md` pattern
  (e.g. `api-stability.md`, `testing-patterns.md`).
- Sections within each document are ordered alphabetically, with the exception of any
  introductory content placed directly under the document title.
