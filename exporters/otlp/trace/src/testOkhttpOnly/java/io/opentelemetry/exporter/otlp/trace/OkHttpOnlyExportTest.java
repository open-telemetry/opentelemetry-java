/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import okhttp3.tls.HeldCertificate;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OkHttpOnlyExportTest {

  private static final List<SpanData> SPANS =
      Collections.singletonList(
          TestSpanData.builder()
              .setName("name")
              .setKind(SpanKind.CLIENT)
              .setStartEpochNanos(1)
              .setEndEpochNanos(2)
              .setStatus(StatusData.ok())
              .setHasEnded(true)
              .build());

  private static final HeldCertificate HELD_CERTIFICATE;

  static {
    try {
      HELD_CERTIFICATE =
          new HeldCertificate.Builder()
              .commonName("localhost")
              .addSubjectAlternativeName(InetAddress.getByName("localhost").getCanonicalHostName())
              .build();
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Error building certificate.", e);
    }
  }

  @RegisterExtension
  @Order(2)
  public static ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          sb.service(
              OtlpGrpcSpanExporterBuilder.GRPC_ENDPOINT_PATH,
              new AbstractUnaryGrpcService() {
                @Override
                protected CompletionStage<byte[]> handleMessage(
                    ServiceRequestContext ctx, byte[] message) {
                  return CompletableFuture.completedFuture(
                      ExportTraceServiceResponse.getDefaultInstance().toByteArray());
                }
              });
          sb.http(0);
          sb.https(0);
          sb.tls(HELD_CERTIFICATE.keyPair().getPrivate(), HELD_CERTIFICATE.certificate());
        }
      };

  // NB: Armeria does not support decompression without using the actual grpc-java (naturally
  // this is the same for grpc-java as a test server). The failure does indicate compression, or at
  // least some sort of data transformation was attempted. Separate integration tests using the
  // OTel collector verify that this is indeed correct compression.
  @Test
  void gzipCompressionExportAttemptedButFails() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:" + server.httpPort())
            .setCompression("gzip")
            .build();

    // See note on test method on why this checks isFalse.
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
  }

  @Test
  void plainTextExport() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setEndpoint("http://localhost:" + server.httpPort()).build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void authorityWithAuth() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://foo:bar@localhost:" + server.httpPort())
            .build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport() throws Exception {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .setTrustedCertificates(
                HELD_CERTIFICATE.certificatePem().getBytes(StandardCharsets.UTF_8))
            .build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport_untrusted() {
    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("https://localhost:" + server.httpsPort())
            .build();
    assertThat(exporter.export(SPANS).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
  }

  @Test
  void tlsBadCert() {
    assertThatThrownBy(
            () ->
                OtlpGrpcSpanExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8))
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Could not set trusted certificates");
  }

  @Test
  void usingOkhttp() {
    assertThat(OtlpGrpcSpanExporterBuilder.USE_OKHTTP).isTrue();
  }
}
