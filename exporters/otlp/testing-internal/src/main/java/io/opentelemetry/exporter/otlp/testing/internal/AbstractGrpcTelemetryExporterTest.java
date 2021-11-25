/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.linecorp.armeria.common.grpc.protocol.ArmeriaStatusException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.assertj.core.api.iterable.ThrowingExtractor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractGrpcTelemetryExporterTest<T, U extends Message> {

  private static final ConcurrentLinkedQueue<Object> exportedResourceTelemetry =
      new ConcurrentLinkedQueue<>();

  private static final ConcurrentLinkedQueue<ArmeriaStatusException> grpcErrors =
      new ConcurrentLinkedQueue<>();

  private static final AtomicInteger attempts = new AtomicInteger();

  @RegisterExtension
  static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              "/opentelemetry.proto.collector.trace.v1.TraceService/Export",
              new CollectorService<>(
                  ExportTraceServiceRequest::parseFrom,
                  ExportTraceServiceRequest::getResourceSpansList,
                  ExportTraceServiceResponse.getDefaultInstance().toByteArray()));
          sb.service(
              "/opentelemetry.proto.collector.metrics.v1.MetricsService/Export",
              new CollectorService<>(
                  ExportMetricsServiceRequest::parseFrom,
                  ExportMetricsServiceRequest::getResourceMetricsList,
                  ExportMetricsServiceResponse.getDefaultInstance().toByteArray()));
          sb.service(
              "/opentelemetry.proto.collector.logs.v1.LogsService/Export",
              new CollectorService<>(
                  ExportLogsServiceRequest::parseFrom,
                  ExportLogsServiceRequest::getResourceLogsList,
                  ExportLogsServiceResponse.getDefaultInstance().toByteArray()));

          sb.decorator(LoggingService.newDecorator());
        }
      };

  private static class CollectorService<T> extends AbstractUnaryGrpcService {
    private final ThrowingExtractor<byte[], T, InvalidProtocolBufferException> parse;
    private final Function<T, List<? extends Object>> getResourceTelemetry;
    private final byte[] successResponse;

    private CollectorService(
        ThrowingExtractor<byte[], T, InvalidProtocolBufferException> parse,
        Function<T, List<? extends Object>> getResourceTelemetry,
        byte[] successResponse) {
      this.parse = parse;
      this.getResourceTelemetry = getResourceTelemetry;
      this.successResponse = successResponse;
    }

    @Override
    protected CompletionStage<byte[]> handleMessage(ServiceRequestContext ctx, byte[] message) {
      attempts.incrementAndGet();
      final T request;
      try {
        request = parse.extractThrows(message);
      } catch (InvalidProtocolBufferException e) {
        throw new UncheckedIOException(e);
      }
      exportedResourceTelemetry.addAll(getResourceTelemetry.apply(request));
      ArmeriaStatusException grpcError = grpcErrors.poll();
      if (grpcError != null) {
        throw grpcError;
      }
      return CompletableFuture.completedFuture(successResponse);
    }
  }

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OkHttpGrpcExporter.class);

  private final String type;
  private final U resourceTelemetryInstance;

  private TelemetryExporter<T> exporter;

  protected AbstractGrpcTelemetryExporterTest(String type, U resourceTelemetryInstance) {
    this.type = type;
    this.resourceTelemetryInstance = resourceTelemetryInstance;
  }

  @BeforeAll
  void setUp() {
    exporter = exporterBuilder().setEndpoint(server.httpUri().toString()).build();
  }

  @AfterAll
  void tearDown() {
    exporter.shutdown();
  }

  @AfterEach
  void reset() {
    exportedResourceTelemetry.clear();
    grpcErrors.clear();
    attempts.set(0);
  }

  @Test
  void export() {
    List<T> telemetry = Collections.singletonList(generateFakeTelemetry());
    assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    List<U> expectedResourceTelemetry = toProto(telemetry);
    assertThat(exportedResourceTelemetry).containsExactlyElementsOf(expectedResourceTelemetry);
  }

  @Test
  void multipleItems() {
    List<T> telemetry = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      telemetry.add(generateFakeTelemetry());
    }
    assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    List<U> expectedResourceTelemetry = toProto(telemetry);
    assertThat(exportedResourceTelemetry).containsExactlyElementsOf(expectedResourceTelemetry);
  }

  @Test
  void deadlineSetPerExport() throws InterruptedException {
    TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri().toString())
            .setTimeout(Duration.ofMillis(1500))
            .build();
    try {
      TimeUnit.MILLISECONDS.sleep(2000);
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void exportAfterShutdown() {
    TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri().toString()).build();
    exporter.shutdown();
    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
  }

  @Test
  void doubleShutdown() {
    TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri().toString()).build();
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void error() {
    addGrpcError(13, null);
    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export "
                + type
                + "s. Server responded with gRPC status code 13. Error message:");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  @Test
  void errorWithMessage() {
    addGrpcError(8, "out of quota");
    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export "
                + type
                + "s. Server responded with gRPC status code 8. Error message: out of quota");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  @Test
  void errorWithEscapedMessage() {
    addGrpcError(5, "クマ🐻");
    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export "
                + type
                + "s. Server responded with gRPC status code 5. Error message: クマ🐻");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  @Test
  void testExport_Unavailable() {
    addGrpcError(14, null);
    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export "
                + type
                + "s. Server is UNAVAILABLE. "
                + "Make sure your collector is running and reachable from this network.");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @Test
  void testExport_Unimplemented() {
    addGrpcError(12, "UNIMPLEMENTED");
    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    LoggingEvent log =
        logs.assertContains(
            "Failed to export "
                + type
                + "s. Server responded with UNIMPLEMENTED. "
                + "This usually means that your collector is not configured with an otlp "
                + "receiver in the \"pipelines\" section of the configuration. "
                + "Full error message: UNIMPLEMENTED");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 4, 8, 10, 11, 14, 15})
  void retryableError(int code) {
    addGrpcError(code, null);

    TelemetryExporter<T> exporter = retryingExporter();

    try {
      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isTrue();
    } finally {
      exporter.shutdown();
    }

    assertThat(attempts).hasValue(2);
  }

  @Test
  void retryableError_tooManyAttempts() {
    addGrpcError(1, null);
    addGrpcError(1, null);

    TelemetryExporter<T> exporter = retryingExporter();

    try {
      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }

    assertThat(attempts).hasValue(2);
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 3, 5, 6, 7, 9, 12, 13, 16})
  void nonRetryableError(int code) {
    addGrpcError(code, null);

    TelemetryExporter<T> exporter = retryingExporter();

    try {
      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isFalse();
    } finally {
      exporter.shutdown();
    }

    assertThat(attempts).hasValue(1);
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void validConfig() {
    assertThatCode(() -> exporterBuilder().setTimeout(0, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setTimeout(Duration.ofMillis(0)))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setTimeout(10, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setTimeout(Duration.ofMillis(10)))
        .doesNotThrowAnyException();

    assertThatCode(() -> exporterBuilder().setEndpoint("http://localhost:4317"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("http://foo:bar@localhost"))
        .doesNotThrowAnyException();

    assertThatCode(() -> exporterBuilder().setCompression("gzip")).doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setCompression("none")).doesNotThrowAnyException();

    assertThatCode(() -> exporterBuilder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                exporterBuilder().setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings({"PreferJavaTimeOverload", "NullAway"})
  void invalidConfig() {
    assertThatThrownBy(() -> exporterBuilder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> exporterBuilder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> exporterBuilder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> exporterBuilder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> exporterBuilder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> exporterBuilder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> exporterBuilder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> exporterBuilder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> exporterBuilder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }

  protected abstract TelemetryExporterBuilder<T> exporterBuilder();

  protected abstract T generateFakeTelemetry();

  protected abstract Marshaler[] toMarshalers(List<T> telemetry);

  private List<U> toProto(List<T> telemetry) {
    return Arrays.stream(toMarshalers(telemetry))
        .map(
            marshaler -> {
              ByteArrayOutputStream bos = new ByteArrayOutputStream();
              try {
                marshaler.writeBinaryTo(bos);
                @SuppressWarnings("unchecked")
                U result =
                    (U)
                        resourceTelemetryInstance
                            .newBuilderForType()
                            .mergeFrom(bos.toByteArray())
                            .build();
                return result;
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            })
        .collect(Collectors.toList());
  }

  private TelemetryExporter<T> retryingExporter() {
    return exporterBuilder()
        .setEndpoint(server.httpUri().toString())
        .addRetryPolicy(
            RetryPolicy.builder()
                .setMaxAttempts(2)
                // We don't validate backoff time itself in these tests, just that retries
                // occur. Keep the tests fast by using minimal backoff.
                .setInitialBackoff(Duration.ofNanos(1))
                .setMaxBackoff(Duration.ofNanos(1))
                .setBackoffMultiplier(1)
                .build())
        .build();
  }

  private static void addGrpcError(int code, @Nullable String message) {
    grpcErrors.add(new ArmeriaStatusException(code, message));
  }
}
