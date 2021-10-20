/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.grpc.protocol.AbstractUnaryGrpcService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporterBuilder;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataBuilder;
import io.opentelemetry.sdk.resources.Resource;
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

  private static final long START_NS = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
  private static final List<MetricData> METRICS =
      Collections.singletonList(
          MetricDataBuilder.createLongSum(
              Resource.empty(),
              InstrumentationLibraryInfo.empty(),
              "name",
              "description",
              "1",
              LongSumData.create(
                  /* isMonotonic= */ true,
                  AggregationTemporality.CUMULATIVE,
                  Collections.singletonList(
                      LongPointData.create(
                          START_NS,
                          START_NS + TimeUnit.MILLISECONDS.toNanos(900),
                          Attributes.of(stringKey("k"), "v"),
                          5)))));

  private static final HeldCertificate HELD_CERTIFICATE;
  private static final String canonicalHostName;

  static {
    try {
      canonicalHostName = InetAddress.getByName("localhost").getCanonicalHostName();
      HELD_CERTIFICATE =
          new HeldCertificate.Builder()
              .commonName("localhost")
              .addSubjectAlternativeName(canonicalHostName)
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
              OtlpGrpcMetricExporterBuilder.GRPC_ENDPOINT_PATH,
              new AbstractUnaryGrpcService() {
                @Override
                protected CompletionStage<byte[]> handleMessage(
                    ServiceRequestContext ctx, byte[] message) {
                  return CompletableFuture.completedFuture(
                      ExportMetricsServiceResponse.getDefaultInstance().toByteArray());
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
  void gzipCompressionExportButFails() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://" + canonicalHostName + ":" + server.httpPort())
            .setCompression("gzip")
            .build();
    // See note on test method on why this checks isFalse.
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
  }

  @Test
  void plainTextExport() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://" + canonicalHostName + ":" + server.httpPort())
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void authorityWithAuth() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://foo:bar@" + canonicalHostName + ":" + server.httpPort())
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("https://" + canonicalHostName + ":" + server.httpsPort())
            .setTrustedCertificates(
                HELD_CERTIFICATE.certificatePem().getBytes(StandardCharsets.UTF_8))
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  @Test
  void testTlsExport_untrusted() {
    OtlpGrpcMetricExporter exporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("https://" + canonicalHostName + ":" + server.httpsPort())
            .build();
    assertThat(exporter.export(METRICS).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
  }

  @Test
  void tlsBadCert() {
    assertThatThrownBy(
            () ->
                OtlpGrpcMetricExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8))
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Could not set trusted certificates");
  }

  @Test
  void usingOkhttp() {
    assertThat(OtlpGrpcMetricExporter.builder().delegate)
        .isInstanceOf(OkHttpGrpcExporterBuilder.class);
  }
}
