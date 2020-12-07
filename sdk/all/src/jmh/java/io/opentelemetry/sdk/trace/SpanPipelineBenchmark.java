/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@State(Scope.Benchmark)
public class SpanPipelineBenchmark {
  private static SpanBuilderSdk spanBuilderSdk;
  private static final DockerImageName OTLP_COLLECTOR_IMAGE =
      DockerImageName.parse("otel/opentelemetry-collector-dev:latest");
  private static final int EXPOSED_PORT = 5678;
  private static final int HEALTH_CHECK_PORT = 13133;

  @Setup(Level.Trial)
  public final void setup() {
    // Configuring the collector test-container
    GenericContainer<?> collector =
        new GenericContainer<>(OTLP_COLLECTOR_IMAGE)
            .withExposedPorts(EXPOSED_PORT, HEALTH_CHECK_PORT)
            .waitingFor(Wait.forHttp("/").forPort(HEALTH_CHECK_PORT))
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("/otel.yaml"), "/etc/otel.yaml")
            .withCommand("--config /etc/otel.yaml");

    collector.start();

    String address = collector.getHost() + ":" + collector.getMappedPort(EXPOSED_PORT);

    TracerSdkProvider tracerProvider = TracerSdkProvider.builder().build();

    SimpleSpanProcessor spanProcessor =
        SimpleSpanProcessor.builder(
                OtlpGrpcSpanExporter.builder().setEndpoint(address).setDeadlineMs(50000).build())
            .build();

    tracerProvider.addSpanProcessor(spanProcessor);

    TraceConfig alwaysOn =
        tracerProvider.getActiveTraceConfig().toBuilder().setSampler(Sampler.alwaysOn()).build();
    tracerProvider.updateActiveTraceConfig(alwaysOn);

    Tracer tracerSdk = tracerProvider.get("PipelineBenchmarkTracer");
    spanBuilderSdk = (SpanBuilderSdk) tracerSdk.spanBuilder("PipelineBenchmarkSpan");
  }

  @Benchmark
  @Threads(value = 1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 15, time = 1)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void runThePipeline_01Threads() {
    doWork();
  }

  private static void doWork() {
    Span span = spanBuilderSdk.startSpan();
    for (int i = 0; i < 10; i++) {
      span.setAttribute("benchmarkAttribute_" + i, "benchmarkAttrValue_" + i);
    }
    span.end();
  }
}
