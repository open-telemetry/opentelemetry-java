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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** A testing tool. */
public class OtlpPipelineDriver {

  public static final String COLLECTOR_PROXY_PORT = "44444";

  /** Shut up, checkstyle. */
  @SuppressWarnings({"FutureReturnValueIgnored", "CatchAndPrintStackTrace"})
  public static void main(String[] args) throws InterruptedException, IOException {
    ToxiproxyClient toxiproxyClient = new ToxiproxyClient();
    toxiproxyClient.reset();
    Proxy collectorProxy = toxiproxyClient.getProxyOrNull("collector");

    if (collectorProxy == null) {
      collectorProxy =
          toxiproxyClient.createProxy(
              "collector", "0.0.0.0:" + COLLECTOR_PROXY_PORT, "otel-collector:55680");
    }
    collectorProxy.enable();

    ToxicList toxics = collectorProxy.toxics();
    Latency latency = toxics.latency("jittery_latency", ToxicDirection.DOWNSTREAM, 100);
    latency.setJitter(50);

    for (Toxic toxic : toxiproxyClient.getProxy("collector").toxics().getAll()) {
      System.out.println("toxic = " + toxic.getName() + " : " + toxic.getToxicity());
    }

    IntervalMetricReader intervalMetricReader = setupSdk();
    addOtlpSpanExporter();

    // warm up with a fixed 1000 spans
    runOnce(1000, 0);
    Thread.sleep(2000);

    // spawn threads that will each run for an interval of time
    int numberOfThreads = 8;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(1);
    for (int i = 0; i < numberOfThreads; i++) {
      executorService.submit(
          () -> {
            try {
              latch.await();
              runOnce(null, 10000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          });
    }
    latch.countDown();
    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.MINUTES);

    // sleep for a bit to let everything settle
    Thread.sleep(2000);

    OpenTelemetrySdk.getTracerProvider().shutdown();
    intervalMetricReader.shutdown();

    Thread.sleep(2000);
    toxiproxyClient.reset();
    collectorProxy.delete();
  }

  private static void runOnce(Integer numberOfSpans, int numberOfMillisToRunFor)
      throws InterruptedException {
    Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.perf");
    long start = System.currentTimeMillis();
    int i = 0;
    while (numberOfSpans == null
        ? System.currentTimeMillis() - start < numberOfMillisToRunFor
        : i < numberOfSpans) {
      //    for (int i = 0; i < 10000; i++) {
      Span exampleSpan = tracer.spanBuilder("exampleSpan").startSpan();
      try (Scope scope = tracer.withSpan(exampleSpan)) {
        exampleSpan.setAttribute("exampleNumber", i++);
        exampleSpan.setAttribute("attribute0", "attvalue-0");
        exampleSpan.setAttribute("attribute1", "attvalue-1");
        exampleSpan.setAttribute("attribute2", "attvalue-2");
        exampleSpan.setAttribute("attribute3", "attvalue-3");
        exampleSpan.setAttribute("attribute4", "attvalue-4");
        exampleSpan.setAttribute("attribute5", "attvalue-5");
        exampleSpan.setAttribute("attribute6", "attvalue-6");
        exampleSpan.setAttribute("attribute7", "attvalue-7");
        exampleSpan.setAttribute("attribute8", "attvalue-8");
        exampleSpan.setAttribute("attribute9", "attvalue-9");
        exampleSpan.addEvent("pre-sleep");
        Thread.sleep(1);
      } finally {
        exampleSpan.end();
      }
    }
  }

  private static IntervalMetricReader setupSdk() {
    // this will make sure that a proper service.name attribute is set on all the spans/metrics.
    // note: this is not something you should generally do in code, but should be provided on the
    // command-line. This is here to make the example more self-contained.
    System.setProperty(
        "otel.resource.attributes", "service.name=PerfTester,service.version=1.0.1-RC-1");

    // set up the metric exporter and wire it into the SDK and a timed reader.
    MetricExporter metricExporter =
        new MetricExporter() {
          @Override
          public CompletableResultCode export(Collection<MetricData> metrics) {
            metrics.forEach(
                metricData -> {
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
            // no-op
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

  private static void addOtlpSpanExporter() {
    // set up the span exporter and wire it into the SDK
    OtlpGrpcSpanExporter spanExporter =
        OtlpGrpcSpanExporter.newBuilder()
            .setEndpoint("localhost:" + COLLECTOR_PROXY_PORT)
            .setDeadlineMs(2000)
            .build();
    BatchSpanProcessor spanProcessor =
        BatchSpanProcessor.newBuilder(spanExporter)
            .setMaxQueueSize(10000)
            .setMaxExportBatchSize(1024)
            .setScheduleDelayMillis(1000)
            .build();
    OpenTelemetrySdk.getTracerProvider().addSpanProcessor(spanProcessor);
  }

  private OtlpPipelineDriver() {}
}
