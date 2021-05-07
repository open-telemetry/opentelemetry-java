# SDK autoconfiguration example

This is a simple example that demonstrates the usage of
the [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)
module.

## Prerequisites

* Java 1.8

## How to run

First build this example application:

```shell
../gradlew shadowJar
```

Then start the example application with the logging exporter configured:

```shell
java -Dotel.traces.exporter=logging \
  -cp build/libs/opentelemetry-examples-autoconfigure-0.1.0-SNAPSHOT-all.jar \
  io.opentelemetry.example.autoconfigure.AutoConfigExample
```

Alternatively, instead of system properties you can use environment variables:

```shell
export OTEL_TRACES_EXPORTER=logging

java -cp build/libs/opentelemetry-examples-autoconfigure-0.1.0-SNAPSHOT-all.jar \
  io.opentelemetry.example.autoconfigure.AutoConfigExample
```

Full documentation of all supported properties can be found in
the [OpenTelemetry SDK Autoconfigure README](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure).

After running the app you should see the trace printed out in the console:

```
...
INFO: 'important work' : ca3938a5793f6f9aba5c757f536a50cb b5e826c981112198 INTERNAL [tracer: io.opentelemetry.example.autoconfigure.AutoConfigExample:] AttributesMap{data={foo=42, bar=a string!}, capacity=128, totalAddedValues=2}
...
```

Congratulations! You are now collecting traces using OpenTelemetry.