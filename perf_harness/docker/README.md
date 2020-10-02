# OpenTelemetry Collector Demo

*IMPORTANT:* This uses a pre-released version of the OpenTelemetry Collector.

This demo uses `docker-compose` and by default runs against the
`otel/opentelemetry-collector-dev:latest` image. To run the demo, switch
to this directory and run:

```shell
docker-compose up -d
```

The demo exposes the following backends:

- Jaeger at http://0.0.0.0:16686
- Zipkin at http://0.0.0.0:9411
- Prometheus at http://0.0.0.0:9090 

Notes:

- It may take some time for the application metrics to appear on the Prometheus
 dashboard;

To clean up any docker container from the demo run `docker-compose down` from 
this directory.
