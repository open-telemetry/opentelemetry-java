/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.internal.grpc.UpstreamGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporter;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import okio.Buffer;
import okio.GzipSource;
import okio.Okio;
import org.assertj.core.api.iterable.ThrowingExtractor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractHttpTelemetryExporterTest<T, U extends Message> {

  private static final ConcurrentLinkedQueue<Object> exportedResourceTelemetry =
      new ConcurrentLinkedQueue<>();

  private static final ConcurrentLinkedQueue<HttpResponse> httpErrors =
      new ConcurrentLinkedQueue<>();

  private static final AtomicInteger attempts = new AtomicInteger();

  private static final ConcurrentLinkedQueue<HttpRequest> httpRequests =
      new ConcurrentLinkedQueue<>();

  @RegisterExtension
  @Order(1)
  static final SelfSignedCertificateExtension certificate = new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  static final SelfSignedCertificateExtension clientCertificate =
      new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(3)
  static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              "/v1/traces",
              new CollectorService<>(
                  ExportTraceServiceRequest::parseFrom,
                  ExportTraceServiceRequest::getResourceSpansList,
                  ExportTraceServiceResponse.getDefaultInstance().toByteArray()));
          sb.service(
              "/v1/metrics",
              new CollectorService<>(
                  ExportMetricsServiceRequest::parseFrom,
                  ExportMetricsServiceRequest::getResourceMetricsList,
                  ExportMetricsServiceResponse.getDefaultInstance().toByteArray()));
          sb.service(
              "/v1/logs",
              new CollectorService<>(
                  ExportLogsServiceRequest::parseFrom,
                  ExportLogsServiceRequest::getResourceLogsList,
                  ExportLogsServiceResponse.getDefaultInstance().toByteArray()));

          sb.http(0);
          sb.https(0);
          sb.tls(certificate.certificateFile(), certificate.privateKeyFile());
          sb.tlsCustomizer(ssl -> ssl.trustManager(clientCertificate.certificate()));
          sb.decorator(LoggingService.newDecorator());
        }
      };

  private static class CollectorService<T> implements HttpService {
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
    public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) {
      httpRequests.add(ctx.request());
      attempts.incrementAndGet();
      CompletableFuture<HttpResponse> responseFuture =
          req.aggregate()
              .thenApply(
                  aggReq -> {
                    T request;
                    try {
                      byte[] requestBody =
                          maybeGzipInflate(aggReq.headers(), aggReq.content().array());
                      request = parse.extractThrows(requestBody);
                    } catch (IOException e) {
                      throw new UncheckedIOException(e);
                    }
                    exportedResourceTelemetry.addAll(getResourceTelemetry.apply(request));
                    HttpResponse errorResponse = httpErrors.poll();
                    return errorResponse != null
                        ? errorResponse
                        : HttpResponse.of(
                            HttpStatus.OK,
                            MediaType.parse("application/x-protobuf"),
                            successResponse);
                  });
      return HttpResponse.from(responseFuture);
    }

    private static byte[] maybeGzipInflate(RequestHeaders requestHeaders, byte[] content)
        throws IOException {
      if (!requestHeaders.contains("content-encoding", "gzip")) {
        return content;
      }
      Buffer buffer = new Buffer();
      GzipSource gzipSource = new GzipSource(Okio.source(new ByteArrayInputStream(content)));
      gzipSource.read(buffer, Integer.MAX_VALUE);
      return buffer.readByteArray();
    }
  }

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(OkHttpExporter.class);

  private final String type;
  private final String path;
  private final U resourceTelemetryInstance;

  private TelemetryExporter<T> exporter;

  protected AbstractHttpTelemetryExporterTest(
      String type, String path, U resourceTelemetryInstance) {
    this.type = type;
    this.path = path;
    this.resourceTelemetryInstance = resourceTelemetryInstance;
  }

  @BeforeAll
  void setUp() {
    exporter = exporterBuilder().setEndpoint(server.httpUri() + path).build();

    // Sanity check that TLS files are in PEM format.
    assertThat(certificate.certificateFile())
        .binaryContent()
        .asString(StandardCharsets.UTF_8)
        .startsWith("-----BEGIN CERTIFICATE-----");
    assertThat(certificate.privateKeyFile())
        .binaryContent()
        .asString(StandardCharsets.UTF_8)
        .startsWith("-----BEGIN PRIVATE KEY-----");
    assertThat(clientCertificate.certificateFile())
        .binaryContent()
        .asString(StandardCharsets.UTF_8)
        .startsWith("-----BEGIN CERTIFICATE-----");
    assertThat(clientCertificate.privateKeyFile())
        .binaryContent()
        .asString(StandardCharsets.UTF_8)
        .startsWith("-----BEGIN PRIVATE KEY-----");
  }

  @AfterAll
  void tearDown() {
    exporter.shutdown();
  }

  @AfterEach
  void reset() {
    exportedResourceTelemetry.clear();
    httpErrors.clear();
    attempts.set(0);
    httpRequests.clear();
  }

  @Test
  void export() {
    List<T> telemetry = Collections.singletonList(generateFakeTelemetry());
    assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    List<U> expectedResourceTelemetry = toProto(telemetry);
    assertThat(exportedResourceTelemetry).containsExactlyElementsOf(expectedResourceTelemetry);

    // Assert request contains OTLP spec compliant User-Agent header
    assertThat(httpRequests)
        .singleElement()
        .satisfies(
            req ->
                assertThat(req.headers().get("User-Agent"))
                    .matches("OTel-OTLP-Exporter-Java/1\\..*"));
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
  void compressionWithNone() {
    TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri() + path).setCompression("none").build();
    assertThat(exporter.unwrap()).extracting("delegate.compressionEnabled").isEqualTo(false);
    try {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("content-encoding")).isNull());
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionWithGzip() {
    TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri() + path).setCompression("gzip").build();
    // UpstreamGrpcExporter doesn't support compression, so we skip the assertion
    assumeThat(exporter.unwrap())
        .extracting("delegate")
        .isNotInstanceOf(UpstreamGrpcExporter.class);
    assertThat(exporter.unwrap()).extracting("delegate.compressionEnabled").isEqualTo(true);
    try {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("content-encoding")).isEqualTo("gzip"));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void authorityWithAuth() {
    TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint("http://foo:bar@localhost:" + server.httpPort() + path)
            .build();
    try {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void withHeaders() {
    TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri() + path).addHeader("key", "value").build();
    try {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("key")).isEqualTo("value"));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void tls() throws Exception {
    TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpsUri() + path)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .build();
    try {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  @SuppressLogger(OkHttpExporter.class)
  void tls_untrusted() {
    TelemetryExporter<T> exporter = exporterBuilder().setEndpoint(server.httpsUri() + path).build();
    try {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void tls_badCert() {
    assertThatThrownBy(
            () ->
                exporterBuilder()
                    .setEndpoint(server.httpsUri() + path)
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Error creating X509TrustManager with provided certs");
  }

  @ParameterizedTest
  @ArgumentsSource(ClientPrivateKeyProvider.class)
  void clientTls(byte[] privateKey) throws Exception {
    TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpsUri() + path)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .setClientTls(
                privateKey, Files.readAllBytes(clientCertificate.certificateFile().toPath()))
            .build();
    try {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    } finally {
      exporter.shutdown();
    }
  }

  private static class ClientPrivateKeyProvider implements ArgumentsProvider {
    @Override
    @SuppressWarnings("PrimitiveArrayPassedToVarargsMethod")
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
          arguments(named("PEM", Files.readAllBytes(clientCertificate.privateKeyFile().toPath()))),
          arguments(named("DER", clientCertificate.privateKey().getEncoded())));
    }
  }

  @Test
  void deadlineSetPerExport() throws InterruptedException {
    TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri() + path)
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
  @SuppressLogger(OkHttpExporter.class)
  void exportAfterShutdown() {
    TelemetryExporter<T> exporter = exporterBuilder().setEndpoint(server.httpUri() + path).build();
    exporter.shutdown();
    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(httpRequests).isEmpty();
  }

  @Test
  @SuppressLogger(OkHttpExporter.class)
  void doubleShutdown() {
    TelemetryExporter<T> exporter = exporterBuilder().setEndpoint(server.httpUri() + path).build();
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  @SuppressLogger(OkHttpExporter.class)
  void error() {
    addHttpError(500);
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
                + "s. Server responded with HTTP status code 500. Error message:");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  @ParameterizedTest
  @ValueSource(ints = {429, 502, 503, 504})
  void retryableError(int code) {
    addHttpError(code);

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
  @SuppressLogger(OkHttpExporter.class)
  void retryableError_tooManyAttempts() {
    addHttpError(502);
    addHttpError(502);

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
  @SuppressLogger(OkHttpExporter.class)
  @ValueSource(ints = {400, 401, 403, 500, 501})
  void nonRetryableError(int code) {
    addHttpError(code);

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

    assertThatCode(() -> exporterBuilder().setEndpoint("http://localhost:4318"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("http://localhost/"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("https://localhost/"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("http://foo:bar@localhost/"))
        .doesNotThrowAnyException();

    assertThatCode(() -> exporterBuilder().setCompression("gzip")).doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setCompression("none")).doesNotThrowAnyException();

    assertThatCode(() -> exporterBuilder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () -> exporterBuilder().setTrustedCertificates(certificate.certificate().getEncoded()))
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
        .setEndpoint(server.httpUri() + path)
        .setRetryPolicy(
            RetryPolicy.builder()
                .setMaxAttempts(2)
                // We don't validate backoff time itself in these tests, just that retries
                // occur. Keep the tests fast by using minimal backoff.
                .setInitialBackoff(Duration.ofMillis(1))
                .setMaxBackoff(Duration.ofMillis(1))
                .setBackoffMultiplier(1)
                .build())
        .build();
  }

  private static void addHttpError(int code) {
    httpErrors.add(HttpResponse.of(code));
  }
}
