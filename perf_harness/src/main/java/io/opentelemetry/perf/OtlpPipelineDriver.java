/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.perf;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicList;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.logging.LoggingMetricExporter;
import io.opentelemetry.exporters.otlp.OtlpGrpcMetricExporter;
import io.opentelemetry.exporters.otlp.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class OtlpPipelineDriver {

  public static final String COLLECTOR_PROXY_PORT = "44444";

  public static void main(String[] args) throws InterruptedException, IOException {
    ToxiproxyClient toxiproxyClient = new ToxiproxyClient();
    toxiproxyClient.reset();
    Proxy collectorProxy = toxiproxyClient.getProxyOrNull("collector");

    if (collectorProxy == null) {
      collectorProxy = toxiproxyClient
          .createProxy("collector", "0.0.0.0:" + COLLECTOR_PROXY_PORT, "otel-collector:55680");
    }
    collectorProxy.enable();

    ToxicList toxics = collectorProxy.toxics();
    Latency latency = toxics.latency("jittery_latency", ToxicDirection.DOWNSTREAM, 800);
    latency.setJitter(10);

    for (Toxic toxic : toxiproxyClient.getProxy("collector").toxics().getAll()) {
      System.out.println("toxic = " + toxic.getName() + " : " + toxic.getToxicity());
    }

    IntervalMetricReader intervalMetricReader = setupSdk();

    Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.perf");

    for (int i = 0; i < 10000; i++) {
      Span exampleSpan = tracer.spanBuilder("exampleSpan").startSpan();
      try (Scope scope = tracer.withSpan(exampleSpan)) {
        exampleSpan.setAttribute("good", "true");
        exampleSpan.setAttribute("exampleNumber", i);
        Thread.sleep(1);
      } finally {
        exampleSpan.end();
      }
    }

    // sleep for a bit to let everything settle
    Thread.sleep(2000);

    OpenTelemetrySdk.getTracerProvider().shutdown();
    intervalMetricReader.shutdown();

    Thread.sleep(2000);
    toxiproxyClient.reset();
    collectorProxy.delete();
  }

  private static IntervalMetricReader setupSdk() {
    // this will make sure that a proper service.name attribute is set on all the spans/metrics.
    // note: this is not something you should generally do in code, but should be provided on the
    // command-line. This is here to make the example more self-contained.
    System.setProperty("otel.resource.attributes", "service.name=OtlpExporterExample");

    // set up the span exporter and wire it into the SDK
    OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.newBuilder()
        .setEndpoint("localhost:" + COLLECTOR_PROXY_PORT)
        .setDeadlineMs(2000)
        .build();
    BatchSpanProcessor spanProcessor =
        BatchSpanProcessor.newBuilder(spanExporter).setScheduleDelayMillis(1000).build();
    OpenTelemetrySdk.getTracerProvider().addSpanProcessor(spanProcessor);

    // set up the metric exporter and wire it into the SDK and a timed reader.
    MetricExporter metricExporter = new MetricExporter() {
      @Override
      public CompletableResultCode export(Collection<MetricData> metrics) {
        metrics.forEach(metricData -> {
          System.out.println("metricData.getDescriptor() = " + metricData.getDescriptor());
          metricData.getPoints().forEach(point -> System.out.println("\tpoint = " + point));
        });
        return CompletableResultCode.ofSuccess();
      }

      @Override
      public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
      }

      @Override
      public void shutdown() {
        //no-op
      }
    };

    IntervalMetricReader intervalMetricReader =
        IntervalMetricReader.builder()
            .setMetricExporter(metricExporter)
            .setMetricProducers(
                Collections.singleton(OpenTelemetrySdk.getMeterProvider().getMetricProducer()))
            .setExportIntervalMillis(1000)
            .build();
    return intervalMetricReader;
  }
}
