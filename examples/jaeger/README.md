# Jaeger Example

This is a simple example that demonstrates how to use the OpenTelemetry SDK 
to instrument a simple application using Jaeger as trace exporter. 

# How to run

## Prerequisites
* Java 1.8.231
* Docker 19.03
* Jaeger 1.16 - [Link][jaeger]


## 1 - Compile 
```bash
gradlew fatJar
```
## 2 - Run Jaeger

```bash
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.16
```


## 3 - Start the Application
```bash
java -cp build/libs/opentelemetry-example-jaeger-all-0.2.0.jar io.opentelemetry.example.Main localhost 6832
```
## 4 - Open the Jaeger UI

Navigate to http://localhost:16686

[jaeger]:[https://www.jaegertracing.io/docs/1.16/getting-started/
