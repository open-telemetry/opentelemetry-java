# SDK Usage Examples

This is a simple example that demonstrates how to use and configure the OpenTelemetry SDK. 

## Prerequisites
* Java 1.8 or higher


## Compile
Compile with 
```shell script
../gradlew fatJar
```

## Run

The following commands are used to run the examples.
```shell script
java -cp build/libs/opentelemetry-examples-sdk-usage-all-0.1.0-SNAPSHOT.jar io.opentelemetry.sdk.example.ConfigureTraceExample
```
```shell script
java -cp build/libs/opentelemetry-examples-sdk-usage-all-0.1.0-SNAPSHOT.jar io.opentelemetry.sdk.example.ConfigureSpanProcessorExample
```