---
title: "Instrumentation Examples"
weight: 4
---

Here are Some of the resources for Opentelemetry Instrumentation Examples

## Community Resources

### boot-opentelemetry-tempo

Project demonstrating Complete Observability Stack utilizing [Prometheus](https://prometheus.io/), [Loki](https://grafana.com/oss/loki/) (_For distributed logging_), [Tempo](https://grafana.com/oss/tempo/) (_For Distributed tracing, this basically uses Jaeger Internally_), [Grafana](https://grafana.com/grafana/) for **Java/spring** based applications (_With OpenTelemetry auto / manual Instrumentation_) involving multiple microservices with DB interactions

Checkout [boot-opentelemetry-tempo](https://github.com/mnadeem/boot-opentelemetry-tempo) and get started

````bash
mvn clean package docker:build
````

````bash
docker-compose up
````


