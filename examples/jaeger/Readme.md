# Jaeger Example

This is a simple example that demonstrates how to use the OpenTelemetry SDK 
to instrument a simple application using Jaeger as trace exporter. 

# How to run

## Prerequisites
* Java 1.8.231

## 1 - Compile 
```bash
gradlew fatJar
```

## 2 - Start the Application
```bash
java -cp build/libs/opentelemetry-example-jaeger-all-0.3.0-SNAPSHOT.jar io.opentelemetry.example.Main localhost 6832
```
