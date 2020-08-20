# OpenTelemetry Resource Providers

This package includes some standard `ResourceProvider`s for filling in attributes related to
common environments. Currently the resources provide the following semantic conventions

## Populated attributes

### Operating System

Provider: `io.opentelemetry.sdk.extensions.resources.OsResource`

Specification: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/resource/semantic_conventions/os.md

Implemented attributes:
- `os.name`
- `os.description`

### Process

Implementation: `io.opentelemetry.sdk.extensions.resources.ProcessResource`

Specification: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/resource/semantic_conventions/process.md

Implemented attributes:
- `process.pid`
- `process.executable.path` (note, we assume the `java` binary is located in the `bin` subfolder of `JAVA_HOME`)
- `process.command_line` (note this includes all system properties and arguments when running)

## Platforms

This package currently does not run on Android. It has been verified on OpenJDK and should work on
other server JVM distributions but if you find any issues please let us know.