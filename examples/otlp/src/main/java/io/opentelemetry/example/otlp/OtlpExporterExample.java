/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.otlp;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * Example code for setting up the OTLP exporters.
 *
 * <p>If you wish to use this code, you'll need to run a copy of the collector locally, on the
 * default port. There is a docker-compose configuration for doing this in the docker subdirectory
 * of this module.
 */
public class OtlpExporterExample {

  public static void main(String[] args) throws InterruptedException {
    // this will make sure that a proper service.name attribute is set on all the spans/metrics.
    // note: this is not something you should generally do in code, but should be provided on the
    // command-line. This is here to make the example more self-contained.
    System.setProperty("otel.resource.attributes", "service.name=OtlpExporterExample");

    // it is important to initialize your SDK as early as possible in your application's lifecycle
    OpenTelemetry openTelemetry = ExampleConfiguration.initOpenTelemetry();
    // note: currently metrics is alpha and the configuration story is still unfolding. This will
    // definitely change in the future.
    MeterProvider meterProvider = ExampleConfiguration.initOpenTelemetryMetrics();

    Tracer tracer = openTelemetry.getTracer("io.opentelemetry.example");
    Meter meter = meterProvider.get("io.opentelemetry.example");
    LongCounter counter = meter.longCounterBuilder("example_counter").build();

    for (int i = 0; i < 10; i++) {
      Span exampleSpan = tracer.spanBuilder("exampleSpan").startSpan();
      try (Scope scope = exampleSpan.makeCurrent()) {
        counter.add(1);
        exampleSpan.setAttribute("good", "true");
        exampleSpan.setAttribute("exampleNumber", i);
        Thread.sleep(100);
      } finally {
        exampleSpan.end();
      }
    }

    // sleep for a bit to let everything settle
    Thread.sleep(2000);
  }
}
