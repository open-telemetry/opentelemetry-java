/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.common.TlsKeyPair;
import com.linecorp.armeria.common.grpc.protocol.ArmeriaStatusException;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.SimpleDecoratingHttpService;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.grpc.Decompressor;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.exporter.internal.FailedExportException;
import io.opentelemetry.exporter.internal.TlsUtil;
import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.profiles.v1development.ExportProfilesServiceRequest;
import io.opentelemetry.proto.collector.profiles.v1development.ExportProfilesServiceResponse;
import io.opentelemetry.proto.collector.profiles.v1development.ProfilesServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.GrpcResponse;
import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
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
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractGrpcTelemetryExporterTest<T, U extends Message> {

  private static final ConcurrentLinkedQueue<Object> exportedResourceTelemetry =
      new ConcurrentLinkedQueue<>();

  private static final ConcurrentLinkedQueue<GrpcServerResponse> grpcResponses =
      new ConcurrentLinkedQueue<>();

  private static volatile byte[] defaultResponseBytes = new byte[0];

  private static final AtomicInteger attempts = new AtomicInteger();

  private static final ConcurrentLinkedQueue<HttpRequest> httpRequests =
      new ConcurrentLinkedQueue<>();

  private static final AtomicInteger grpcEncodingServerAttempts = new AtomicInteger();

  private static volatile String grpcEncodingServerEncoding = "brotli";

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
              GrpcService.builder()
                  .addService(
                      new LogsServiceGrpc.LogsServiceImplBase() {
                        @Override
                        public void export(
                            ExportLogsServiceRequest request,
                            StreamObserver<ExportLogsServiceResponse> responseObserver) {
                          exportedResourceTelemetry.addAll(request.getResourceLogsList());
                          handleExport(responseObserver, ExportLogsServiceResponse.parser());
                        }
                      })
                  .addService(
                      new MetricsServiceGrpc.MetricsServiceImplBase() {
                        @Override
                        public void export(
                            ExportMetricsServiceRequest request,
                            StreamObserver<ExportMetricsServiceResponse> responseObserver) {
                          exportedResourceTelemetry.addAll(request.getResourceMetricsList());
                          handleExport(responseObserver, ExportMetricsServiceResponse.parser());
                        }
                      })
                  .addService(
                      new TraceServiceGrpc.TraceServiceImplBase() {
                        @Override
                        public void export(
                            ExportTraceServiceRequest request,
                            StreamObserver<ExportTraceServiceResponse> responseObserver) {
                          exportedResourceTelemetry.addAll(request.getResourceSpansList());
                          handleExport(responseObserver, ExportTraceServiceResponse.parser());
                        }
                      })
                  .addService(
                      new ProfilesServiceGrpc.ProfilesServiceImplBase() {
                        @Override
                        public void export(
                            ExportProfilesServiceRequest request,
                            StreamObserver<ExportProfilesServiceResponse> responseObserver) {
                          exportedResourceTelemetry.addAll(request.getResourceProfilesList());
                          handleExport(responseObserver, ExportProfilesServiceResponse.parser());
                        }
                      })
                  .decompressorRegistry(
                      DecompressorRegistry.getDefaultInstance()
                          .with(
                              new Decompressor() {
                                @Override
                                public String getMessageEncoding() {
                                  return "base64";
                                }

                                @Override
                                public InputStream decompress(InputStream is) {
                                  return Base64.getDecoder().wrap(is);
                                }
                              },
                              true))
                  .build(),
              service ->
                  new SimpleDecoratingHttpService(service) {
                    @Override
                    public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req)
                        throws Exception {
                      httpRequests.add(req);
                      attempts.incrementAndGet();
                      return unwrap().serve(ctx, req);
                    }
                  });

          sb.http(0);
          sb.https(0);
          sb.tls(TlsKeyPair.of(certificate.privateKey(), certificate.certificate()));
          sb.tlsCustomizer(ssl -> ssl.trustManager(clientCertificate.certificate()));
          // Uncomment for detailed request / response logs from server
          // sb.decorator(LoggingService.newDecorator());
        }
      };

  private static <R extends Message> void handleExport(
      StreamObserver<R> responseObserver, Parser<R> parser) {
    GrpcServerResponse grpcResponse = grpcResponses.poll();
    if (grpcResponse != null && grpcResponse.error != null) {
      responseObserver.onError(grpcResponse.error);
    } else {
      try {
        byte[] responseBytes =
            grpcResponse != null && grpcResponse.responseBytes != null
                ? grpcResponse.responseBytes
                : defaultResponseBytes;
        responseObserver.onNext(parser.parseFrom(responseBytes));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      responseObserver.onCompleted();
    }
  }

  // A minimal server that returns a configurable grpc-encoding to test encoding handling.
  // The encoding is controlled via grpcEncodingServerEncoding (defaults to "brotli").
  // Both OkHttpGrpcSender (our code) and UpstreamGrpcSender (grpc-java framework) reject unknown
  // encodings and fail the export with INTERNAL status.
  @RegisterExtension
  @Order(4)
  static final ServerExtension grpcEncodingServer =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              "prefix:/",
              (ctx, req) -> {
                grpcEncodingServerAttempts.incrementAndGet();
                // gRPC frame: compressed flag=1, declared length=0 (no payload bytes follow)
                byte[] frame = {1, 0, 0, 0, 0};
                return HttpResponse.of(
                    ResponseHeaders.builder(HttpStatus.OK)
                        .contentType(MediaType.parse("application/grpc+proto"))
                        .add("grpc-encoding", grpcEncodingServerEncoding)
                        .add("grpc-status", "0")
                        .build(),
                    HttpData.wrap(frame));
              });
          sb.http(0);
        }
      };

  @RegisterExtension
  protected LogCapturer logs = LogCapturer.create().captureForType(GrpcExporter.class);

  private final String type;
  private final U resourceTelemetryInstance;

  private TelemetryExporter<T> exporter;

  protected AbstractGrpcTelemetryExporterTest(String type, U resourceTelemetryInstance) {
    this.type = type;
    this.resourceTelemetryInstance = resourceTelemetryInstance;
  }

  @BeforeAll
  void setUp() {
    exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri().toString())
            // We don't validate backoff time itself in these tests, just that retries
            // occur. Keep the tests fast by using minimal backoff.
            .setRetryPolicy(
                RetryPolicy.getDefault().toBuilder()
                    .setMaxAttempts(2)
                    .setInitialBackoff(Duration.ofMillis(1))
                    .build())
            .build();
    defaultResponseBytes = exporter.exportResponse(0).toByteArray();

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
    grpcResponses.clear();
    attempts.set(0);
    httpRequests.clear();
    grpcEncodingServerAttempts.set(0);
    grpcEncodingServerEncoding = "brotli";
  }

  @Test
  void minimalChannel() {
    // Test that UpstreamGrpcSender uses minimal fallback managed channel, so skip for
    // OkHttpGrpcSender
    assumeThat(exporter.unwrap())
        .extracting("delegate.grpcSender")
        .matches(sender -> sender.getClass().getSimpleName().equals("UpstreamGrpcSender"));
    // When no channel is explicitly set, should fall back to a minimally configured managed channel
    try (TelemetryExporter<?> exporter = exporterBuilder().build()) {
      assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
      assertThat(exporter.unwrap())
          .extracting(
              "delegate.grpcSender.channel",
              as(InstanceOfAssertFactories.type(ManagedChannel.class)))
          .satisfies(channel -> assertThat(channel.isShutdown()).isTrue());
    }
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
  // @SuppressLogger(HttpExporter.class)
  void responseBodyBounds() {
    // We have a 4mb hardcoded response body limit. Responses <= 4mb succeed. Responses >= 4mb
    // fail. We can't test payloads exactly at 4mb because protobuf message lengths are finicky -
    // its hard to create a message with an exact size.

    // Body below the limit, succeeds
    addGrpcResponse(GrpcStatusCode.OK, null, exporter.exportResponse(100));
    CompletableResultCode result =
        exporter
            .export(Collections.singletonList(generateFakeTelemetry()))
            .join(10, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isTrue();

    // Body over the limit, fails with RESOURCE_EXHAUSTED
    addGrpcResponse(GrpcStatusCode.OK, null, exporter.exportResponse(4 * 1024 * 1024 + 1));
    result =
        exporter
            .export(Collections.singletonList(generateFakeTelemetry()))
            .join(10, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getFailureThrowable())
        .isInstanceOfSatisfying(
            FailedExportException.GrpcExportException.class,
            ex ->
                assertThat(requireNonNull(ex.getResponse()))
                    .isNotNull()
                    .satisfies(
                        grpcResponse ->
                            assertThat(grpcResponse.getStatusCode())
                                .isEqualTo(GrpcStatusCode.RESOURCE_EXHAUSTED)));
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void responseBodyBoundsGzipCompressed() throws IOException {
    // Verify the body size limit is applied to the decompressed bytes, not the compressed wire
    // bytes. The server compresses the response with gzip (because the sender advertises
    // grpc-accept-encoding: gzip), so the wire bytes are well under 4MB while the decompressed
    // bytes exceed it.
    AbstractMessageLite<?, ?> responseMsg = exporter.exportResponse(4 * 1024 * 1024 + 1);
    byte[] responseBytes = responseMsg.toByteArray();
    // Sanity: confirm payload is over the limit uncompressed but compresses well under the limit
    assertThat(responseBytes).hasSizeGreaterThan(4 * 1024 * 1024);
    ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(compressedBaos)) {
      gzip.write(responseBytes);
    }
    assertThat(compressedBaos.toByteArray()).hasSizeLessThan(4 * 1024 * 1024);

    addGrpcResponse(GrpcStatusCode.OK, null, responseMsg);
    CompletableResultCode result =
        exporter
            .export(Collections.singletonList(generateFakeTelemetry()))
            .join(10, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getFailureThrowable())
        .isInstanceOfSatisfying(
            FailedExportException.GrpcExportException.class,
            ex ->
                assertThat(requireNonNull(ex.getResponse()))
                    .satisfies(
                        grpcResponse ->
                            assertThat(grpcResponse.getStatusCode())
                                .isEqualTo(GrpcStatusCode.RESOURCE_EXHAUSTED)));
  }

  // Verifies that a response with an unrecognized grpc-encoding fails with INTERNAL rather than
  // attempting to decompress with an unsupported algorithm.
  @Test
  @SuppressLogger(GrpcExporter.class)
  void unsupportedGrpcMessageEncoding() {
    try (TelemetryExporter<T> testExporter =
        exporterBuilder()
            .setEndpoint(grpcEncodingServer.httpUri().toString())
            .setRetryPolicy(null)
            .build()) {
      CompletableResultCode result =
          testExporter
              .export(Collections.singletonList(generateFakeTelemetry()))
              .join(10, TimeUnit.SECONDS);
      assertThat(result.isSuccess()).isFalse();
      assertThat(grpcEncodingServerAttempts).hasValue(1);
      assertThat(result.getFailureThrowable())
          .isInstanceOfSatisfying(
              FailedExportException.GrpcExportException.class,
              ex ->
                  assertThat(requireNonNull(ex.getResponse()))
                      .satisfies(
                          grpcResponse ->
                              assertThat(grpcResponse.getStatusCode())
                                  .isEqualTo(GrpcStatusCode.INTERNAL)));
    }
  }

  // Verifies that a response with flag=1 and grpc-encoding: identity is rejected with INTERNAL.
  // Although identity is advertised in grpc-accept-encoding (meaning "no encoding applied"),
  // a server setting the compressed flag to 1 is contradictory and should not be accepted.
  @Test
  @SuppressLogger(GrpcExporter.class)
  void identityGrpcMessageEncodingWithCompressedFlagRejected() {
    grpcEncodingServerEncoding = "identity";
    try (TelemetryExporter<T> testExporter =
        exporterBuilder()
            .setEndpoint(grpcEncodingServer.httpUri().toString())
            .setRetryPolicy(null)
            .build()) {
      CompletableResultCode result =
          testExporter
              .export(Collections.singletonList(generateFakeTelemetry()))
              .join(10, TimeUnit.SECONDS);
      assertThat(result.isSuccess()).isFalse();
      assertThat(grpcEncodingServerAttempts).hasValue(1);
      assertThat(result.getFailureThrowable())
          .isInstanceOfSatisfying(
              FailedExportException.GrpcExportException.class,
              ex ->
                  assertThat(requireNonNull(ex.getResponse()))
                      .satisfies(
                          grpcResponse ->
                              assertThat(grpcResponse.getStatusCode())
                                  .isEqualTo(GrpcStatusCode.INTERNAL)));
    }
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
        exporterBuilder().setEndpoint(server.httpUri().toString()).setCompression("none").build()) {

      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("grpc-encoding")).isNull());
    }
  }

  @Test
  void compressionWithGzip() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri().toString()).setCompression("gzip").build()) {

      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("grpc-encoding")).isEqualTo("gzip"));
    }
  }

  @Test
  void compressionWithSpiCompressor() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri().toString())
            .setCompression("base64")
            .build()) {
      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isTrue();
      assertThat(httpRequests)
          .singleElement()
          .satisfies(req -> assertThat(req.headers().get("grpc-encoding")).isEqualTo("base64"));
    }
  }

  @Test
  void authorityWithAuth() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint("http://foo:bar@localhost:" + server.httpPort()).build()) {
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
            .setEndpoint(server.httpUri().toString())
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
            .setTrustedCertificates(Files.readAllBytes(certificate.certificateFile().toPath()))
            .setClientTls(
                certificate.privateKey().getEncoded(), certificate.certificate().getEncoded())
            .setEndpoint(server.httpsUri().toString())
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
            .setSslContext(sslContext, trustManager)
            .setEndpoint(server.httpsUri().toString())
            .build()) {
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void tls_untrusted() {
    try (TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpsUri().toString()).build()) {
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
                    .setEndpoint(server.httpsUri().toString())
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Error creating X509TrustManager with provided certs.");
  }

  @ParameterizedTest
  @ArgumentsSource(ClientPrivateKeyProvider.class)
  void clientTls(byte[] privateKey) throws Exception {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpsUri().toString())
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
  @SuppressLogger(GrpcExporter.class)
  void connectTimeout() {
    // UpstreamGrpcSender doesn't support connectTimeout, so we skip the test
    assumeThat(exporter.unwrap())
        .extracting("delegate.grpcSender")
        .matches(sender -> sender.getClass().getSimpleName().equals("OkHttpGrpcSender"));

    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            // Connecting to a non-routable IP address to trigger connection error
            .setEndpoint("http://10.255.255.1")
            .setConnectTimeout(Duration.ofMillis(1))
            .setRetryPolicy(null)
            .build()) {
      long startTimeMillis = System.currentTimeMillis();
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));

      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isFalse();

      assertThat(result.getFailureThrowable())
          .asInstanceOf(
              InstanceOfAssertFactories.throwable(FailedExportException.GrpcExportException.class))
          .returns(false, Assertions.from(FailedExportException::failedWithResponse))
          .satisfies(
              ex -> {
                assertThat(ex.getResponse()).isNull();
                assertThat(ex.getCause()).isNotNull();
              });

      // Assert that the export request fails well before the default connect timeout of 10s
      // Note: Connection failures to non-routable IPs can take 1-5 seconds depending on OS/network
      assertThat(System.currentTimeMillis() - startTimeMillis)
          .isLessThan(TimeUnit.SECONDS.toMillis(6));
    }
  }

  @Test
  void deadlineSetPerExport() throws InterruptedException {
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri().toString())
            .setTimeout(Duration.ofMillis(1500))
            .build()) {
      TimeUnit.MILLISECONDS.sleep(2000);
      CompletableResultCode result =
          exporter.export(Collections.singletonList(generateFakeTelemetry()));
      assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
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
    assertThat(httpRequests).isEmpty();
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void doubleShutdown() {
    TelemetryExporter<T> exporter =
        exporterBuilder().setEndpoint(server.httpUri().toString()).build();
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void error() {
    GrpcStatusCode statusCode = GrpcStatusCode.INTERNAL;
    addGrpcResponse(statusCode, null);

    try (TelemetryExporter<T> exporter = nonRetryingExporter()) {
      CompletableResultCode result =
          exporter
              .export(Collections.singletonList(generateFakeTelemetry()))
              .join(10, TimeUnit.SECONDS);

      assertThat(result.isSuccess()).isFalse();

      assertThat(result.getFailureThrowable())
          .asInstanceOf(
              InstanceOfAssertFactories.throwable(FailedExportException.GrpcExportException.class))
          .returns(true, Assertions.from(FailedExportException::failedWithResponse))
          .satisfies(
              ex -> {
                assertThat(ex.getResponse())
                    .isNotNull()
                    .extracting(GrpcResponse::getStatusCode)
                    .isEqualTo(statusCode);

                assertThat(ex.getCause()).isNull();
              });

      LoggingEvent log =
          logs.assertContains(
              "Failed to export "
                  + type
                  + "s. Server responded with gRPC status code 13. Error message:");
      assertThat(log.getLevel()).isEqualTo(Level.WARN);
    }
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void errorWithUnknownError() {
    addGrpcResponse(GrpcStatusCode.UNKNOWN, null);

    try (TelemetryExporter<T> exporter = nonRetryingExporter()) {
      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .getFailureThrowable())
          .asInstanceOf(
              InstanceOfAssertFactories.throwable(FailedExportException.GrpcExportException.class))
          .returns(true, Assertions.from(FailedExportException::failedWithResponse))
          .satisfies(
              ex -> {
                assertThat(ex.getResponse()).isNotNull();

                assertThat(ex.getCause()).isNull();
              });
    }
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void errorWithMessage() {
    addGrpcResponse(GrpcStatusCode.RESOURCE_EXHAUSTED, "out of quota");

    try (TelemetryExporter<T> exporter = nonRetryingExporter()) {
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
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void errorWithEscapedMessage() {
    addGrpcResponse(GrpcStatusCode.NOT_FOUND, "クマ🐻");

    try (TelemetryExporter<T> exporter = nonRetryingExporter()) {
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
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void testExport_Unavailable() {
    addGrpcResponse(GrpcStatusCode.UNAVAILABLE, null);

    try (TelemetryExporter<T> exporter = nonRetryingExporter()) {
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
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  protected void testExport_Unimplemented() {
    addGrpcResponse(GrpcStatusCode.UNIMPLEMENTED, "UNIMPLEMENTED");

    try (TelemetryExporter<T> exporter = nonRetryingExporter()) {
      assertThat(
              exporter
                  .export(Collections.singletonList(generateFakeTelemetry()))
                  .join(10, TimeUnit.SECONDS)
                  .isSuccess())
          .isFalse();
      String envVar;
      switch (type) {
        case "span":
          envVar = "OTEL_TRACES_EXPORTER";
          break;
        case "metric":
          envVar = "OTEL_METRICS_EXPORTER";
          break;
        case "log":
          envVar = "OTEL_LOGS_EXPORTER";
          break;
        case "profile":
          envVar = "OTEL_PROFILES_EXPORTER";
          break;
        default:
          throw new AssertionError();
      }
      LoggingEvent log =
          logs.assertContains(
              "Failed to export "
                  + type
                  + "s. Server responded with UNIMPLEMENTED. "
                  + "This usually means that your collector is not configured with an otlp "
                  + "receiver in the \"pipelines\" section of the configuration. "
                  + "If export is not desired and you are using OpenTelemetry autoconfiguration or the javaagent, "
                  + "disable export by setting "
                  + envVar
                  + "=none. "
                  + "Full error message: UNIMPLEMENTED");
      assertThat(log.getLevel()).isEqualTo(Level.ERROR);
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 4, 8, 10, 11, 14, 15})
  @SuppressLogger(GrpcExporter.class)
  void retryableError(int code) {
    addGrpcResponse(GrpcStatusCode.fromValue(code), null);

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isTrue();

    assertThat(attempts).hasValue(2);
  }

  @Test
  @SuppressLogger(GrpcExporter.class)
  void retryableError_tooManyAttempts() {
    addGrpcResponse(GrpcStatusCode.CANCELLED, null);
    addGrpcResponse(GrpcStatusCode.CANCELLED, null);

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();

    assertThat(attempts).hasValue(2);
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 3, 5, 6, 7, 9, 12, 13, 16})
  @SuppressLogger(GrpcExporter.class)
  void nonRetryableError(int code) {
    addGrpcResponse(GrpcStatusCode.fromValue(code), null);

    assertThat(
            exporter
                .export(Collections.singletonList(generateFakeTelemetry()))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();

    assertThat(attempts).hasValue(1);
  }

  @Test
  void overrideHost() {
    List<T> telemetry = Collections.singletonList(generateFakeTelemetry());
    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri().toString())
            .addHeader("host", "opentelemetry")
            .build()) {
      assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    }
    List<U> expectedResourceTelemetry = toProto(telemetry);
    assertThat(exportedResourceTelemetry).containsExactlyElementsOf(expectedResourceTelemetry);

    assertThat(httpRequests)
        .singleElement()
        .satisfies(req -> assertThat(req.authority()).isEqualTo("opentelemetry"));
  }

  @Test
  void executorService() {
    ExecutorServiceSpy executorService =
        new ExecutorServiceSpy(Executors.newSingleThreadExecutor());

    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setEndpoint(server.httpUri().toString())
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

    assertThatCode(() -> exporterBuilder().setEndpoint("http://localhost:4317"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> exporterBuilder().setEndpoint("http://foo:bar@localhost"))
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

  @Test
  void customServiceClassLoader() {
    ClassLoaderSpy classLoaderSpy =
        new ClassLoaderSpy(AbstractHttpTelemetryExporterTest.class.getClassLoader());

    try (TelemetryExporter<T> exporter =
        exporterBuilder()
            .setServiceClassLoader(classLoaderSpy)
            .setEndpoint(server.httpUri().toString())
            .build()) {
      assertThat(classLoaderSpy.getResourcesNames)
          .isEqualTo(
              Collections.singletonList(
                  "META-INF/services/io.opentelemetry.sdk.common.export.GrpcSenderProvider"));
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
            .setEndpoint("http://localhost:4317")
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
  void stringRepresentation() throws IOException, CertificateEncodingException {
    try (TelemetryExporter<T> telemetryExporter =
        exporterBuilder().setEndpoint("http://localhost:4317").build()) {
      assertThat(telemetryExporter.unwrap().toString())
          .matches(
              "OtlpGrpc[a-zA-Z]*Exporter\\{"
                  + "endpoint=http://localhost:4317, "
                  + "fullMethodName=.*, "
                  + "timeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "connectTimeoutNanos="
                  + TimeUnit.SECONDS.toNanos(10)
                  + ", "
                  + "compressorEncoding=null, "
                  + "headers=Headers\\{User-Agent=OBFUSCATED\\}"
                  + ".*" // Maybe additional grpcChannel field, signal specific fields
                  + "\\}");
    }

    try (TelemetryExporter<T> telemetryExporter =
        exporterBuilder()
            .setTimeout(Duration.ofSeconds(5))
            .setConnectTimeout(Duration.ofSeconds(4))
            .setEndpoint("http://example:4317")
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
            .build(); ) {
      assertThat(telemetryExporter.unwrap().toString())
          .matches(
              "OtlpGrpc[a-zA-Z]*Exporter\\{"
                  + "endpoint=http://example:4317, "
                  + "fullMethodName=.*, "
                  + "timeoutNanos="
                  + TimeUnit.SECONDS.toNanos(5)
                  + ", "
                  + "connectTimeoutNanos="
                  + TimeUnit.SECONDS.toNanos(4)
                  + ", "
                  + "compressorEncoding=gzip, "
                  + "headers=Headers\\{.*foo=OBFUSCATED.*\\}, "
                  + "retryPolicy=RetryPolicy\\{maxAttempts=2, initialBackoff=PT0\\.05S, maxBackoff=PT3S, backoffMultiplier=1\\.3, retryExceptionPredicate=null\\}"
                  + ".*" // Maybe additional grpcChannel field, signal specific fields
                  + "\\}");
    }
  }

  @Test
  void latestInternalTelemetry() {
    // Profiles do not expose metrics yet, so skip
    assumeThat(type).isNotEqualTo("profile");

    InMemoryMetricReader inMemoryMetrics = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
            SdkMeterProvider.builder().registerMetricReader(inMemoryMetrics).build();
        TelemetryExporter<T> exporter =
            exporterBuilder()
                .setEndpoint(server.httpUri().toString())
                .setMeterProvider(() -> meterProvider)
                .setInternalTelemetryVersion(InternalTelemetryVersion.LATEST)
                .build()) {

      List<T> telemetry = Collections.singletonList(generateFakeTelemetry());
      assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();

      List<AttributeAssertion> expectedAttributes =
          Arrays.asList(
              satisfies(
                  SemConvAttributes.OTEL_COMPONENT_TYPE,
                  str -> str.matches("otlp_grpc_(log|metric|span)_exporter")),
              satisfies(
                  SemConvAttributes.OTEL_COMPONENT_NAME,
                  str -> str.matches("otlp_grpc_(log|metric|span)_exporter/\\d+")),
              satisfies(
                  SemConvAttributes.SERVER_PORT, str -> str.isEqualTo(server.httpUri().getPort())),
              satisfies(
                  SemConvAttributes.SERVER_ADDRESS,
                  str -> str.isEqualTo(server.httpUri().getHost())));

      assertThat(inMemoryMetrics.collectAllMetrics())
          .hasSize(3)
          .anySatisfy(
              metric ->
                  OpenTelemetryAssertions.assertThat(metric)
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
                  OpenTelemetryAssertions.assertThat(metric)
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
                  OpenTelemetryAssertions.assertThat(metric)
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
    // Profiles do not expose metrics yet, so skip
    assumeThat(type).isNotEqualTo("profile");

    InMemoryMetricReader inMemoryMetrics = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
            SdkMeterProvider.builder().registerMetricReader(inMemoryMetrics).build();
        TelemetryExporter<T> exporter =
            exporterBuilder()
                .setEndpoint(server.httpUri().toString())
                .setMeterProvider(() -> meterProvider)
                .setInternalTelemetryVersion(InternalTelemetryVersion.LEGACY)
                .build()) {

      List<T> telemetry = Collections.singletonList(generateFakeTelemetry());
      assertThat(exporter.export(telemetry).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();

      assertThat(inMemoryMetrics.collectAllMetrics())
          .hasSize(2)
          .anySatisfy(
              metric ->
                  OpenTelemetryAssertions.assertThat(metric)
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
                  OpenTelemetryAssertions.assertThat(metric)
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

  protected TelemetryExporter<T> nonRetryingExporter() {
    return exporterBuilder().setEndpoint(server.httpUri().toString()).setRetryPolicy(null).build();
  }

  protected static void addGrpcResponse(GrpcStatusCode code, @Nullable String message) {
    addGrpcResponse(code, message, null);
  }

  protected static void addGrpcResponse(
      GrpcStatusCode code,
      @Nullable String message,
      @Nullable AbstractMessageLite<?, ?> bodyMessage) {
    grpcResponses.add(
        new GrpcServerResponse(
            code == GrpcStatusCode.OK ? null : new ArmeriaStatusException(code.getValue(), message),
            bodyMessage == null ? null : bodyMessage.toByteArray()));
  }

  private static final class GrpcServerResponse {
    @Nullable final ArmeriaStatusException error;
    @Nullable final byte[] responseBytes;

    GrpcServerResponse(@Nullable ArmeriaStatusException error, @Nullable byte[] responseBytes) {
      this.error = error;
      this.responseBytes = responseBytes;
    }
  }
}
