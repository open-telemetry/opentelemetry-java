/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.common.TlsKeyPair;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.FailedExportException;
import io.opentelemetry.exporter.internal.TlsUtil;
import io.opentelemetry.exporter.internal.compression.GzipCompressor;
import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.testing.internal.compressor.Base64Compressor;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import okio.Buffer;
import okio.GzipSource;
import okio.Okio;
import okio.Source;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
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
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockserver.integration.ClientAndServer;
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
          sb.tls(TlsKeyPair.of(certificate.privateKey(), certificate.certificate()));
          sb.tlsCustomizer(ssl -> ssl.trustManager(clientCertificate.certificate()));
          // Uncomment for detailed request / response logs from server
          // sb.decorator(LoggingService.newDecorator());
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
                      byte[] requestBody = maybeInflate(aggReq.headers(), aggReq.content().array());
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
      return HttpResponse.of(responseFuture);
    }

    private static byte[] maybeInflate(RequestHeaders requestHeaders, byte[] content)
        throws IOException {
      if (requestHeaders.contains("content-encoding", "gzip")) {
        Buffer buffer = new Buffer();
        GzipSource gzipSource = new GzipSource(Okio.source(new ByteArrayInputStream(content)));
        gzipSource.read(buffer, Integer.MAX_VALUE);
        return buffer.readByteArray();
      }
      if (requestHeaders.contains("content-encoding", "base64")) {
        Buffer buffer = new Buffer();
        Source base64Source =
            Okio.source(Base64.getDecoder().wrap(new ByteArrayInputStream(content)));
        base64Source.read(buffer, Integer.MAX_VALUE);
        return buffer.readByteArray();
      }
      return content;
    }
  }

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(HttpExporter.class);

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
    //
    exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri() + path)
            // We don't validate backoff time itself in these tests, just that retries
            // occur. Keep the tests fast by using minimal backoff.
            .setRetryPolicy(
                RetryPolicy.getDefault().toBuilder()
                    .setMaxAttempts(2)
                    .setInitialBackoff(Duration.ofMillis(1))
                    .build())
            .build();

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
    try (TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri() + path).setCompression("none").build()) {
      assertThat(exporter.unwrap()).extracting("delegate.httpSender.compressor").isNull();

      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("content-encoding")).isNull());
    }
  }

  @Test
  void compressionWithGzip() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri() + path).setCompression("gzip").build()) {
      assertThat(exporter.unwrap())
          .extracting("delegate.httpSender.compressor")
          .isEqualTo(GzipCompressor.getInstance());

      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("content-encoding")).isEqualTo("gzip"));
    }
  }

  @Test
  void compressionWithSpiCompressor() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri() + path).setCompression("base64").build()) {
      assertThat(exporter.unwrap())
          .extracting("delegate.httpSender.compressor")
          .isEqualTo(Base64Compressor.getInstance());

      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("content-encoding")).isEqualTo("base64"));
    }
  }

  @Test
  void authorityWithAuth() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint("http://foo:bar@localhost:" + server.httpPort() + path)
            .build()) {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
  }

  @Test
  void withHeaders() {
    AtomicInteger count = new AtomicInteger();
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri() + path)
            .addHeader("key1", "value1")
            .setHeaders(() -> Collections.singletonMap("key2", "value" + count.incrementAndGet()))
            .build()) {
      // Export twice to ensure header supplier gets invoked twice
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      result = exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();

      assertThat(httpRequests)
          .satisfiesExactly(
              req -> {
                assertThat(req.headers().get("key1")).isEqualTo("value1");
                assertThat(req.headers().get("key2")).isEqualTo("value" + (count.get() - 1));
              },
              req -> {
                assertThat(req.headers().get("key1")).isEqualTo("value1");
                assertThat(req.headers().get("key2")).isEqualTo("value" + count.get());
              });
    }
  }

  @Test
  void tls() throws Exception {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpsUri() + path)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .build()) {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
  }

  @Test
  void tlsViaSslContext() throws Exception {
    X509TrustManager trustManager = TlsUtil.trustManager(certificate.certificate().getEncoded());

    X509KeyManager keyManager =
        TlsUtil.keyManager(
            certificate.privateKey().getEncoded(), certificate.certificate().getEncoded());

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(new KeyManager[] {keyManager}, new TrustManager[] {trustManager}, null);

    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpsUri() + path)
            .setSslContext(sslContext, trustManager)
            .build()) {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void tls_untrusted() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpsUri() + path).build()) {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
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
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpsUri() + path)
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .setClientTls(
                privateKey, Files.readAllBytes(clientCertificate.certificateFile().toPath()))
            .build()) {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
  }

  private static class ClientPrivateKeyProvider implements ArgumentsProvider {
    @Override
    @SuppressWarnings("PrimitiveArrayPassedToVarargsMethod")
    public Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) throws Exception {
      return Stream.of(
          arguments(named("PEM", Files.readAllBytes(clientCertificate.privateKeyFile().toPath()))),
          arguments(named("DER", clientCertificate.privateKey().getEncoded())));
    }
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void connectTimeout() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            // Connecting to a non-routable IP address to trigger connection error
            .setEndpoint("http://10.255.255.1")
            .setConnectTimeout(Duration.ofMillis(1))
            .setRetryPolicy(null)
            .build()) {
      long startTimeMillis = System.currentTimeMillis();
      CompletableResultCode result =
          exporter
              .export(Collections.singletonList(generateFakeTelemetry()))
              .join(10, TimeUnit.SECONDS);

      assertThat(result.isSuccess()).isFalse();

      assertThat(result.getFailureThrowable())
          .asInstanceOf(
              InstanceOfAssertFactories.throwable(FailedExportException.HttpExportException.class))
          .returns(false, Assertions.from(FailedExportException::failedWithResponse))
          .satisfies(
              ex -> {
                assertThat(ex.getResponse()).isNull();
                assertThat(ex.getCause()).isNotNull();
              });

      // Assert that the export request fails well before the default connect timeout of 10s
      assertThat(System.currentTimeMillis() - startTimeMillis)
          .isLessThan(TimeUnit.SECONDS.toMillis(1));
    }
  }

  @Test
  void deadlineSetPerExport() throws InterruptedException {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri() + path)
            .setTimeout(Duration.ofMillis(1500))
            .build()) {
      TimeUnit.MILLISECONDS.sleep(2000);
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
  }

  @Test
  @SuppressLogger(HttpExporter.class)
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
  @SuppressLogger(HttpExporter.class)
  void doubleShutdown() {
    int logsSizeBefore = logs.getEvents().size();
    TelemetryExporter<T> exporter = exporterBuilder().setEndpoint(server.httpUri() + path).build();
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(logs.getEvents()).hasSize(logsSizeBefore);
    logs.assertDoesNotContain("Calling shutdown() multiple times.");
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void error() {
    int statusCode = 500;
    addHttpError(statusCode);
    CompletableResultCode result =
        exporter
            .export(Collections.singletonList(generateFakeTelemetry()))
            .join(10, TimeUnit.SECONDS);

    assertThat(result.isSuccess()).isFalse();

    assertThat(result.getFailureThrowable())
        .asInstanceOf(
            InstanceOfAssertFactories.throwable(FailedExportException.HttpExportException.class))
        .returns(true, Assertions.from(FailedExportException::failedWithResponse))
        .satisfies(
            ex -> {
              assertThat(ex.getResponse())
                  .isNotNull()
                  .satisfies(
                      response -> {
                        assertThat(response)
                            .extracting(HttpSender.Response::statusCode)
                            .isEqualTo(statusCode);

                        assertThatCode(response::responseBody).doesNotThrowAnyException();
                      });

              assertThat(ex.getCause()).isNull();
            });

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

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();

    assertThat(attempts).hasValue(2);
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void retryableError_tooManyAttempts() {
    addHttpError(502);
    addHttpError(502);

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();

    assertThat(attempts).hasValue(2);
  }

  @ParameterizedTest
  @SuppressLogger(HttpExporter.class)
  @ValueSource(ints = {400, 401, 403, 500, 501})
  void nonRetryableError(int code) {
    addHttpError(code);

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();

    assertThat(attempts).hasValue(1);
  }

  @Test
  void proxy() {
    // configure mockserver to proxy to the local OTLP server
    InetSocketAddress serverSocketAddress = server.httpSocketAddress();
    try (ClientAndServer clientAndServer =
            ClientAndServer.startClientAndServer(
                serverSocketAddress.getHostName(), serverSocketAddress.getPort());
        TelemetryExporter<T> exporter =
            exporterBuilder()
                // Configure exporter with server endpoint, and proxy options to route through
                // mockserver proxy
                .setEndpoint(server.httpUri() + path)
                .setProxyOptions(
                    ProxyOptions.create(
                        InetSocketAddress.createUnresolved("localhost", clientAndServer.getPort())))
                .build()) {

      List<T> telemetry = Collections.singletonList(generateFakeTelemetry());

      assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      // assert that mock server received request
      assertThat(clientAndServer.retrieveRecordedRequests(new org.mockserver.model.HttpRequest()))
          .hasSize(1);
      // assert that server received telemetry from proxy, and is as expected
      List<U> expectedResourceTelemetry = toProto(telemetry);
      assertThat(exportedResourceTelemetry).containsExactlyElementsOf(expectedResourceTelemetry);
    }
  }

  @Test
  void executorService() {
    ExecutorServiceSpy executorService =
        new ExecutorServiceSpy(Executors.newSingleThreadExecutor());

    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri() + path)
            .setExecutorService(executorService)
            .build()) {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));

      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(executorService.getTaskCount()).isPositive();
    } finally {
      // If setting executor, the user is responsible for calling shutdown
      assertThat(executorService.isShutdown()).isFalse();
      executorService.shutdown();
    }
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void validConfig() {
    // We must build exporters to test timeout settings, which intersect with underlying client
    // implementations and may convert between Duration, int, and long, which may be susceptible to
    // overflow exceptions.
    assertThatCode(() -> buildAndShutdown(exporterBuilder().setTimeout(0, TimeUnit.MILLISECONDS)))
        .doesNotThrowAnyException();
    assertThatCode(() -> buildAndShutdown(exporterBuilder().setTimeout(Duration.ofMillis(0))))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                buildAndShutdown(
                    exporterBuilder().setTimeout(Long.MAX_VALUE, TimeUnit.NANOSECONDS)))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> buildAndShutdown(exporterBuilder().setTimeout(Duration.ofNanos(Long.MAX_VALUE))))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> buildAndShutdown(exporterBuilder().setTimeout(Long.MAX_VALUE, TimeUnit.SECONDS)))
        .doesNotThrowAnyException();
    assertThatCode(() -> buildAndShutdown(exporterBuilder().setTimeout(10, TimeUnit.MILLISECONDS)))
        .doesNotThrowAnyException();
    assertThatCode(() -> buildAndShutdown(exporterBuilder().setTimeout(Duration.ofMillis(10))))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> buildAndShutdown(exporterBuilder().setConnectTimeout(0, TimeUnit.MILLISECONDS)))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> buildAndShutdown(exporterBuilder().setConnectTimeout(Duration.ofMillis(0))))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> buildAndShutdown(exporterBuilder().setConnectTimeout(10, TimeUnit.MILLISECONDS)))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> buildAndShutdown(exporterBuilder().setConnectTimeout(Duration.ofMillis(10))))
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
    // SPI compressor available for this test but not packaged with OTLP exporter
    assertThatCode(() -> exporterBuilder().setCompression("base64")).doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setCompression("none")).doesNotThrowAnyException();

    assertThatCode(() -> exporterBuilder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () -> exporterBuilder().setTrustedCertificates(certificate.certificate().getEncoded()))
        .doesNotThrowAnyException();
  }

  private void buildAndShutdown(TelemetryExporterBuilder<T> builder) {
    TelemetryExporter<T> build = builder.build();
    build.shutdown().join(10, TimeUnit.MILLISECONDS);
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
    assertThatThrownBy(
            () ->
                buildAndShutdown(exporterBuilder().setTimeout(Duration.ofSeconds(Long.MAX_VALUE))))
        .isInstanceOf(ArithmeticException.class);

    assertThatThrownBy(() -> exporterBuilder().setConnectTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> exporterBuilder().setConnectTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> exporterBuilder().setConnectTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");
    assertThatThrownBy(
            () ->
                buildAndShutdown(
                    exporterBuilder().setConnectTimeout(Duration.ofSeconds(Long.MAX_VALUE))))
        .isInstanceOf(ArithmeticException.class);

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
            "Unsupported compressionMethod. Compression method must be \"none\" or one of: [base64,gzip]");
  }

  @Test
  void toBuilderEquality()
      throws CertificateEncodingException,
          IOException,
          NoSuchFieldException,
          IllegalAccessException {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setTimeout(Duration.ofSeconds(5))
            .setConnectTimeout(Duration.ofSeconds(4))
            .setEndpoint("http://localhost:4318")
            .setCompression("gzip")
            .addHeader("foo", "bar")
            .setTrustedCertificates(certificate.certificate().getEncoded())
            .setClientTls(
                Files.readAllBytes(clientCertificate.privateKeyFile().toPath()),
                Files.readAllBytes(clientCertificate.certificateFile().toPath()))
            .setRetryPolicy(
                RetryPolicy.builder()
                    .setMaxAttempts(2)
                    .setMaxBackoff(Duration.ofSeconds(3))
                    .setInitialBackoff(Duration.ofMillis(50))
                    .setBackoffMultiplier(1.3)
                    .build())
            .setComponentLoader(ComponentLoader.forClassLoader(new ClassLoader() {}))
            .build()) {
      Object unwrapped = exporter.unwrap();
      Field builderField = unwrapped.getClass().getDeclaredField("builder");
      builderField.setAccessible(true);

      // Builder copy should be equal to original when unchanged
      TelemetryExporter<T> copy = toBuilder(exporter).build();
      try {
        assertThat(copy.unwrap())
            .extracting("builder")
            .usingRecursiveComparison()
            .withStrictTypeChecking()
            .ignoringFields("tlsConfigHelper")
            .isEqualTo(builderField.get(unwrapped));
      } finally {
        copy.shutdown();
      }

      // Builder copy should NOT be equal when changed
      copy = toBuilder(exporter).addHeader("baz", "qux").build();
      try {
        assertThat(copy.unwrap())
            .extracting("builder")
            .usingRecursiveComparison()
            .withStrictTypeChecking()
            .ignoringFields("tlsConfigHelper")
            .isNotEqualTo(builderField.get(unwrapped));
      } finally {
        copy.shutdown();
      }
    }
  }

  @Test
  void customServiceClassLoader() {
    ClassLoaderSpy classLoaderSpy =
        new ClassLoaderSpy(AbstractHttpTelemetryExporterTest.class.getClassLoader());

    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setServiceClassLoader(classLoaderSpy)
            .setEndpoint(server.httpUri() + path)
            .build()) {
      assertThat(classLoaderSpy.getResourcesNames)
          .isEqualTo(
              Collections.singletonList(
                  "META-INF/services/io.opentelemetry.exporter.internal.http.HttpSenderProvider"));
    }
  }

  private static class ClassLoaderSpy extends ClassLoader {
    private final List<String> getResourcesNames = new ArrayList<>();

    private ClassLoaderSpy(ClassLoader delegate) {
      super(delegate);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      getResourcesNames.add(name);
      return super.getResources(name);
    }
  }

  @Test
  void stringRepresentation() throws IOException, CertificateEncodingException {
    try (TelemetryExporter<T> telemetryExporter = exporterBuilder().build(); ) {
      assertThat(telemetryExporter.unwrap().toString())
          .matches(
              "OtlpHttp[a-zA-Z]*Exporter\\{"
                  + "endpoint=http://localhost:4318/v1/[a-zA-Z]*, "
                  + "timeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "proxyOptions=null, "
                  + "compressorEncoding=null, "
                  + "connectTimeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "exportAsJson=false, "
                  + "headers=Headers\\{User-Agent=OBFUSCATED\\}"
                  + ".*" // Maybe additional signal specific fields
                  + "\\}");
    }

    try (TelemetryExporter<T> telemetryExporter =
        exporterBuilder()
            .setTimeout(Duration.ofSeconds(5))
            .setConnectTimeout(Duration.ofSeconds(4))
            .setEndpoint("http://example:4318/v1/logs")
            .setCompression("gzip")
            .addHeader("foo", "bar")
            .setTrustedCertificates(certificate.certificate().getEncoded())
            .setClientTls(
                Files.readAllBytes(clientCertificate.privateKeyFile().toPath()),
                Files.readAllBytes(clientCertificate.certificateFile().toPath()))
            .setRetryPolicy(
                RetryPolicy.builder()
                    .setMaxAttempts(2)
                    .setMaxBackoff(Duration.ofSeconds(3))
                    .setInitialBackoff(Duration.ofMillis(50))
                    .setBackoffMultiplier(1.3)
                    .build())
            .build()) {
      assertThat(telemetryExporter.unwrap().toString())
          .matches(
              "OtlpHttp[a-zA-Z]*Exporter\\{"
                  + "endpoint=http://example:4318/v1/[a-zA-Z]*, "
                  + "timeoutNanos="
                  + TimeUnit.SECONDS.toNanos(5)
                  + ", "
                  + "proxyOptions=null, "
                  + "compressorEncoding=gzip, "
                  + "connectTimeoutNanos="
                  + TimeUnit.SECONDS.toNanos(4)
                  + ", "
                  + "exportAsJson=false, "
                  + "headers=Headers\\{.*foo=OBFUSCATED.*\\}, "
                  + "retryPolicy=RetryPolicy\\{maxAttempts=2, initialBackoff=PT0\\.05S, maxBackoff=PT3S, backoffMultiplier=1\\.3, retryExceptionPredicate=null\\}"
                  + ".*" // Maybe additional signal specific fields
                  + "\\}");
    }
  }

  @Test
  void latestInternalTelemetry() {
    InMemoryMetricReader inMemoryMetrics = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
            SdkMeterProvider.builder().registerMetricReader(inMemoryMetrics).build();
        TelemetryExporter<T> exporter =
            exporterBuilder()
                .setEndpoint(server.httpUri() + path)
                .setMeterProvider(() -> meterProvider)
                .setInternalTelemetryVersion(InternalTelemetryVersion.LATEST)
                .build()) {
      List<T> telemetry = Collections.singletonList(generateFakeTelemetry());
      assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();

      List<AttributeAssertion> expectedAttributes =
          Arrays.asList(
              satisfies(
                  SemConvAttributes.OTEL_COMPONENT_TYPE,
                  str -> str.matches("otlp_http_(log|metric|span)_exporter")),
              satisfies(
                  SemConvAttributes.OTEL_COMPONENT_NAME,
                  str -> str.matches("otlp_http_(log|metric|span)_exporter/\\d+")),
              satisfies(
                  SemConvAttributes.SERVER_PORT, str -> str.isEqualTo(server.httpUri().getPort())),
              satisfies(
                  SemConvAttributes.SERVER_ADDRESS,
                  str -> str.isEqualTo(server.httpUri().getHost())));

      assertThat(inMemoryMetrics.collectAllMetrics())
          .hasSize(3)
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .satisfies(
                          m ->
                              assertThat(m.getName())
                                  .matches(
                                      "otel.sdk.exporter.(span|metric_data_point|log).inflight"))
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa -> pa.hasAttributesSatisfying(expectedAttributes))))
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .satisfies(
                          m ->
                              assertThat(m.getName())
                                  .matches(
                                      "otel.sdk.exporter.(span|metric_data_point|log).exported"))
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa -> pa.hasAttributesSatisfying(expectedAttributes))))
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .hasName("otel.sdk.exporter.operation.duration")
                      .hasHistogramSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa ->
                                      pa.hasAttributesSatisfying(expectedAttributes)
                                          .hasBucketCounts(1))));
    }
  }

  @Test
  void legacyInternalTelemetry() {
    InMemoryMetricReader inMemoryMetrics = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
            SdkMeterProvider.builder().registerMetricReader(inMemoryMetrics).build();
        TelemetryExporter<T> exporter =
            exporterBuilder()
                .setEndpoint(server.httpUri() + path)
                .setMeterProvider(() -> meterProvider)
                .setInternalTelemetryVersion(InternalTelemetryVersion.LEGACY)
                .build()) {
      List<T> telemetry = Collections.singletonList(generateFakeTelemetry());
      assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();

      assertThat(inMemoryMetrics.collectAllMetrics())
          .hasSize(2)
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .hasName("otlp.exporter.seen")
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa ->
                                      pa.hasAttributesSatisfying(
                                          satisfies(
                                              stringKey("type"),
                                              str -> str.matches("log|span|metric"))))))
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .hasName("otlp.exporter.exported")
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa ->
                                      pa.hasAttributesSatisfying(
                                          satisfies(
                                              stringKey("type"),
                                              str -> str.matches("log|span|metric"))))));
    }
  }

  protected abstract TelemetryExporterBuilder<T> exporterBuilder();

  protected abstract TelemetryExporterBuilder<T> toBuilder(TelemetryExporter<T> exporter);

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

  private static void addHttpError(int code) {
    httpErrors.add(HttpResponse.of(code));
  }
}
