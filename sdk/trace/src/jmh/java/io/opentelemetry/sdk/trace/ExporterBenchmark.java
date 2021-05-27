/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
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

public class ExporterBenchmark {
  private ExporterBenchmark() {}

  @State(Scope.Benchmark)
  public abstract static class AbstractProcessorBenchmark {
    private static final DockerImageName OTLP_COLLECTOR_IMAGE =
        DockerImageName.parse("otel/opentelemetry-collector-dev:latest");
    protected static final int OTLP_PORT = 5678;
    protected static final int JAEGER_PORT = 14268;
    private static final int HEALTH_CHECK_PORT = 13133;
    protected SdkSpanBuilder sdkSpanBuilder;

    protected abstract SpanExporter createExporter(GenericContainer<?> collector);

    @Setup(Level.Trial)
    public void setup() {
      // Configuring the collector test-container
      GenericContainer<?> collector =
          new GenericContainer<>(OTLP_COLLECTOR_IMAGE)
              .withExposedPorts(OTLP_PORT, HEALTH_CHECK_PORT, JAEGER_PORT)
              .waitingFor(Wait.forHttp("/").forPort(HEALTH_CHECK_PORT))
              .withCopyFileToContainer(
                  MountableFile.forClasspathResource("/otel.yaml"), "/etc/otel.yaml")
              .withCommand("--config /etc/otel.yaml");

      collector.start();

      SdkTracerProvider tracerProvider =
          SdkTracerProvider.builder()
              .setSampler(Sampler.alwaysOn())
              .addSpanProcessor(SimpleSpanProcessor.create(createExporter(collector)))
              .build();

      Tracer tracerSdk = tracerProvider.get("PipelineBenchmarkTracer");
      sdkSpanBuilder = (SdkSpanBuilder) tracerSdk.spanBuilder("PipelineBenchmarkSpan");
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 10, time = 1)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(1)
    @Threads(1)
    public Span createAndExportSpan() {
      Span span = sdkSpanBuilder.startSpan();
      span.end();
      return span;
    }
  }

  public static class OtlpBenchmark extends AbstractProcessorBenchmark {
    @Override
    protected OtlpGrpcSpanExporter createExporter(GenericContainer<?> collector) {
      String host = collector.getHost();
      int port = collector.getMappedPort(OTLP_PORT);
      return OtlpGrpcSpanExporter.builder()
          .setEndpoint("http://" + host + ":" + port)
          .setTimeout(Duration.ofSeconds(50))
          .build();
    }

  }

  public static class JaegerBenchmark extends AbstractProcessorBenchmark {
    @Override
    protected JaegerThriftSpanExporter createExporter(GenericContainer<?> collector) {
      String host = collector.getHost();
      int port = collector.getMappedPort(JAEGER_PORT);
      return JaegerThriftSpanExporter.builder()
          .setEndpoint("http://" + host + ":" + port + "/api/traces")
          .build();
    }

  }
}
