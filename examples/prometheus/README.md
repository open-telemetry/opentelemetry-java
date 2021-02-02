# Prometheus Example

This example demonstrates how to use the OpenTelemetry SDK 
to instrument a simple application using Prometheus as the metric exporter and expose the metrics via HTTP. 

These are collected by a Prometheus instance which is configured to pull these metrics via HTTP. 

# How to run

## Prerequisites
* Java 1.7
* Docker 19.03

## 1 - Compile 
```shell script
../gradlew shadowJar
```
## 2 - Run Prometheus

Start Prometheus instance with a configuration that sets up a HTTP collection job for  ```127.0.0.1:19090```

See [prometheus.yml](prometheus.yml)

```shell script
docker run --network="host" --rm -it \
    --name prometheus \
    -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
    prom/prometheus 

```

## 3 - Start the Application
```shell script
java -cp build/libs/opentelemetry-examples-prometheus-0.1.0-SNAPSHOT-all.jar io.opentelemetry.example.prometheus.PrometheusExample 19090
```
## 4 - Open the Prometheus UI

Navigate to:

http://localhost:9090/graph?g0.range_input=15m&g0.expr=incoming_messages&g0.tab=0

