/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
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

public class SpanPipelineBenchmark {
  private SpanPipelineBenchmark() {}

  @State(Scope.Benchmark)
  public abstract static class AbstractProcessorBenchmark {
    private static final DockerImageName OTLP_COLLECTOR_IMAGE =
        DockerImageName.parse("otel/opentelemetry-collector-dev:latest");
    private static final int EXPOSED_PORT = 5678;
    private static final int HEALTH_CHECK_PORT = 13133;
    private SpanBuilder sdkSpanBuilder;

    protected abstract SpanProcessor getSpanProcessor(String collectorAddress);

    protected abstract void runThePipeline();

    protected void doWork() {
      Span span = sdkSpanBuilder.startSpan();
      for (int i = 0; i < 10; i++) {
        span.setAttribute("benchmarkAttribute_" + i, "benchmarkAttrValue_" + i);
      }
      span.end();
    }

    @Setup(Level.Trial)
    public void setup() {
      // Configuring the collector test-container
      GenericContainer<?> collector =
          new GenericContainer<>(OTLP_COLLECTOR_IMAGE)
              .withExposedPorts(EXPOSED_PORT, HEALTH_CHECK_PORT)
              .waitingFor(Wait.forHttp("/").forPort(HEALTH_CHECK_PORT))
              .withCopyFileToContainer(
                  MountableFile.forClasspathResource("/otel.yaml"), "/etc/otel.yaml")
              .withCommand("--config /etc/otel.yaml");

      collector.start();

      SpanProcessor spanProcessor = makeSpanProcessor(collector);

      SdkTracerProvider tracerProvider =
          SdkTracerProvider.builder()
              .setSampler(Sampler.alwaysOn())
              .addSpanProcessor(spanProcessor)
              .build();

      Tracer tracerSdk = tracerProvider.get("PipelineBenchmarkTracer");
      sdkSpanBuilder = tracerSdk.spanBuilder("PipelineBenchmarkSpan");
    }

    private SpanProcessor makeSpanProcessor(GenericContainer<?> collector) {
      try {
        String host = collector.getHost();
        Integer port = collector.getMappedPort(EXPOSED_PORT);
        String address = new URL("http", host, port, "").toString();
        return getSpanProcessor(address);
      } catch (MalformedURLException e) {
        throw new IllegalStateException("can't make a url", e);
      }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 15, time = 1)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(1)
    @Threads(1)
    public void measureSpanPipeline() {
      runThePipeline();
    }
  }

  public static class ExecutorServiceSpanProcessorBenchmark extends AbstractProcessorBenchmark {

    @Override
    protected SpanProcessor getSpanProcessor(String collectorAddress) {
      return ExecutorServiceSpanProcessor.builder(
              OtlpGrpcSpanExporter.builder()
                  .setEndpoint(collectorAddress)
                  .setTimeout(Duration.ofSeconds(50))
                  .build(),
              Executors.newSingleThreadScheduledExecutor(),
              true)
          .build();
    }

    @Override
    protected void runThePipeline() {
      doWork();
    }
  }
}
