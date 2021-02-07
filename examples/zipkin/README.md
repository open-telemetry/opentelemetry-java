# Zipkin Example

This is a simple example that demonstrates how to use the OpenTelemetry SDK 
to instrument a simple application using Zipkin as trace exporter. 

# How to run

## Prerequisites
* Java 1.8.231
* Docker 19.03

## 1 - Compile 
```shell script
../gradlew fatJar
```
## 2 - Run Zipkin

```shell script
docker run --rm -it --name zipkin \
  -p 9411:9411 \
  openzipkin/zipkin:2.21
```

## 3 - Start the Application
```shell script
java -cp build/libs/opentelemetry-examples-zipkin-all-0.1.0-SNAPSHOT.jar io.opentelemetry.example.zipkin.ZipkinExample localhost 9411
```
## 4 - Open the Zipkin UI

Navigate to http://localhost:9411/zipkin and click on search.

[zipkin]:[https://zipkin.io/]
