/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Console;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Headers;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric.DefaultHistogramAggregation;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Prometheus;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MetricExporterFactoryTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private SpiHelper spiHelper = SpiHelper.create(MetricExporterFactoryTest.class.getClassLoader());

  @Test
  void create_Null() {
    assertThat(MetricExporterFactory.getInstance().create(null, spiHelper, new ArrayList<>()))
        .isNull();
  }

  @Test
  void create_OtlpDefaults() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    OtlpGrpcMetricExporter expectedExporter = OtlpGrpcMetricExporter.getDefault();
    cleanup.addCloseable(expectedExporter);

    MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .MetricExporter()
                    .withOtlp(new OtlpMetric()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<ConfigProperties> configCaptor = ArgumentCaptor.forClass(ConfigProperties.class);
    verify(spiHelper)
        .loadConfigurable(
            eq(ConfigurableMetricExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.protocol")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.endpoint")).isNull();
    assertThat(configProperties.getMap("otel.exporter.otlp.metrics.headers")).isEmpty();
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.compression")).isNull();
    assertThat(configProperties.getDuration("otel.exporter.otlp.metrics.timeout")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.certificate")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.client.key")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.client.certificate"))
        .isNull();
    assertThat(
            configProperties.getString("otel.exporter.otlp.metrics.default.histogram.aggregation"))
        .isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.temporality.preference"))
        .isNull();
  }

  @Test
  void create_OtlpConfigured(@TempDir Path tempDir)
      throws CertificateEncodingException, IOException {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpMetricExporter expectedExporter =
        OtlpHttpMetricExporter.builder()
            .setEndpoint("http://example:4318/v1/metrics")
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .setTimeout(Duration.ofSeconds(15))
            .setCompression("gzip")
            .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
            .setDefaultAggregationSelector(
                DefaultAggregationSelector.getDefault()
                    .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()))
            .build();
    cleanup.addCloseable(expectedExporter);

    // Write certificates to temp files
    String certificatePath =
        createTempFileWithContent(
            tempDir, "certificate.cert", serverTls.certificate().getEncoded());
    String clientKeyPath =
        createTempFileWithContent(tempDir, "clientKey.key", clientTls.privateKey().getEncoded());
    String clientCertificatePath =
        createTempFileWithContent(
            tempDir, "clientCertificate.cert", clientTls.certificate().getEncoded());

    MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .MetricExporter()
                    .withOtlp(
                        new OtlpMetric()
                            .withProtocol("http/protobuf")
                            .withEndpoint("http://example:4318")
                            .withHeaders(
                                new Headers()
                                    .withAdditionalProperty("key1", "value1")
                                    .withAdditionalProperty("key2", "value2"))
                            .withCompression("gzip")
                            .withTimeout(15_000)
                            .withCertificate(certificatePath)
                            .withClientKey(clientKeyPath)
                            .withClientCertificate(clientCertificatePath)
                            .withTemporalityPreference("delta")
                            .withDefaultHistogramAggregation(
                                DefaultHistogramAggregation.BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM)),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<ConfigProperties> configCaptor = ArgumentCaptor.forClass(ConfigProperties.class);
    verify(spiHelper)
        .loadConfigurable(
            eq(ConfigurableMetricExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.protocol"))
        .isEqualTo("http/protobuf");
    assertThat(configProperties.getString("otel.exporter.otlp.endpoint"))
        .isEqualTo("http://example:4318");
    assertThat(configProperties.getMap("otel.exporter.otlp.metrics.headers"))
        .isEqualTo(ImmutableMap.of("key1", "value1", "key2", "value2"));
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.compression"))
        .isEqualTo("gzip");
    assertThat(configProperties.getDuration("otel.exporter.otlp.metrics.timeout"))
        .isEqualTo(Duration.ofSeconds(15));
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.certificate"))
        .isEqualTo(certificatePath);
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.client.key"))
        .isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.client.certificate"))
        .isEqualTo(clientCertificatePath);
    assertThat(configProperties.getString("otel.exporter.otlp.metrics.temporality.preference"))
        .isEqualTo("delta");
    assertThat(
            configProperties.getString("otel.exporter.otlp.metrics.default.histogram.aggregation"))
        .isEqualTo("base2_exponential_bucket_histogram");
  }

  @Test
  void create_Console() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    LoggingMetricExporter expectedExporter = LoggingMetricExporter.create();
    cleanup.addCloseable(expectedExporter);

    io.opentelemetry.sdk.metrics.export.MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .MetricExporter()
                    .withConsole(new Console()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

  @Test
  void create_PrometheusExporter() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                MetricExporterFactory.getInstance()
                    .create(
                        new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                .MetricExporter()
                            .withPrometheus(new Prometheus()),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("prometheus exporter not supported in this context");
    cleanup.addCloseables(closeables);
  }

  @Test
  void create_SpiExporter() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                MetricExporterFactory.getInstance()
                    .create(
                        new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                .MetricExporter()
                            .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unrecognized metric exporter(s): [test]");
    cleanup.addCloseables(closeables);
  }
}
