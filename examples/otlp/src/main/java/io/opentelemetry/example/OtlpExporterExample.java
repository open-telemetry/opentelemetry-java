/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.example;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

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
    System.setProperty("otel.resource.attributes", "service.name=OtlpExporterExample");
    OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.getDefault();

    SimpleSpanProcessor spanProcessor = SimpleSpanProcessor.newBuilder(spanExporter).build();
    OpenTelemetrySdk.getTracerProvider().addSpanProcessor(spanProcessor);

    // todo: uncomment when OTLP metrics are enabled in the collector
    //    OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.getDefault();
    //    IntervalMetricReader intervalMetricReader = IntervalMetricReader.builder()
    //        .setMetricExporter(metricExporter)
    //        .setMetricProducers(
    //            Collections.singleton(OpenTelemetrySdk.getMeterProvider().getMetricProducer()))
    //        .setExportIntervalMillis(5000)
    //        .build();

    Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.example");

    for (int i = 0; i < 5; i++) {
      Span exampleSpan = tracer.spanBuilder("exampleSpan").startSpan();
      try (Scope scope = tracer.withSpan(exampleSpan)) {
        exampleSpan.setAttribute("good", "true");
        exampleSpan.setAttribute("exampleNumber", i);
        Thread.sleep(100);
      } finally {
        exampleSpan.end();
      }
    }

    OpenTelemetrySdk.getTracerProvider().shutdown();
    //    intervalMetricReader.shutdown();
  }
}
