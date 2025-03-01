/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.MetricExporterComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.NameStringValuePairModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpMetricExporterModel;
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
import java.util.Arrays;
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
  void create_OtlpDefaults() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpMetricExporter expectedExporter = OtlpHttpMetricExporter.getDefault();
    cleanup.addCloseable(expectedExporter);

    MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .PushMetricExporterModel()
                    .withOtlpHttp(new OtlpHttpMetricExporterModel()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<StructuredConfigProperties> configCaptor =
        ArgumentCaptor.forClass(StructuredConfigProperties.class);
    verify(spiHelper)
        .loadComponent(eq(MetricExporter.class), eq("otlp_http"), configCaptor.capture());
    StructuredConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("endpoint")).isNull();
    assertThat(configProperties.getStructured("headers")).isNull();
    assertThat(configProperties.getString("compression")).isNull();
    assertThat(configProperties.getInt("timeout")).isNull();
    assertThat(configProperties.getString("certificate_file")).isNull();
    assertThat(configProperties.getString("client_key_file")).isNull();
    assertThat(configProperties.getString("client_certificate_file")).isNull();
    assertThat(configProperties.getString("temporality_preference")).isNull();
    assertThat(configProperties.getString("default_histogram_aggregation")).isNull();
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
                        .PushMetricExporterModel()
                    .withOtlpHttp(
                        new OtlpHttpMetricExporterModel()
                            .withEndpoint("http://example:4318/v1/metrics")
                            .withHeaders(
                                Arrays.asList(
                                    new NameStringValuePairModel()
                                        .withName("key1")
                                        .withValue("value1"),
                                    new NameStringValuePairModel()
                                        .withName("key2")
                                        .withValue("value2")))
                            .withCompression("gzip")
                            .withTimeout(15_000)
                            .withCertificateFile(certificatePath)
                            .withClientKeyFile(clientKeyPath)
                            .withClientCertificateFile(clientCertificatePath)
                            .withTemporalityPreference(
                                OtlpHttpMetricExporterModel.ExporterTemporalityPreference.DELTA)
                            .withDefaultHistogramAggregation(
                                OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
                                    .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM)),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<StructuredConfigProperties> configCaptor =
        ArgumentCaptor.forClass(StructuredConfigProperties.class);
    verify(spiHelper)
        .loadComponent(eq(MetricExporter.class), eq("otlp_http"), configCaptor.capture());
    StructuredConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://example:4318/v1/metrics");
    List<StructuredConfigProperties> headers = configProperties.getStructuredList("headers");
    assertThat(headers)
        .isNotNull()
        .satisfiesExactly(
            header -> {
              assertThat(header.getString("name")).isEqualTo("key1");
              assertThat(header.getString("value")).isEqualTo("value1");
            },
            header -> {
              assertThat(header.getString("name")).isEqualTo("key2");
              assertThat(header.getString("value")).isEqualTo("value2");
            });
    assertThat(configProperties.getString("compression")).isEqualTo("gzip");
    assertThat(configProperties.getInt("timeout")).isEqualTo(Duration.ofSeconds(15).toMillis());
    assertThat(configProperties.getString("certificate_file")).isEqualTo(certificatePath);
    assertThat(configProperties.getString("client_key_file")).isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("client_certificate_file"))
        .isEqualTo(clientCertificatePath);
    assertThat(configProperties.getString("temporality_preference")).isEqualTo("delta");
    assertThat(configProperties.getString("default_histogram_aggregation"))
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
                        .PushMetricExporterModel()
                    .withConsole(new ConsoleExporterModel()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

  @Test
  void create_SpiExporter_Unknown() {
    assertThatThrownBy(
            () ->
                MetricExporterFactory.getInstance()
                    .create(
                        new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                .PushMetricExporterModel()
                            .withAdditionalProperty(
                                "unknown_key", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.metrics.export.MetricExporter with name \"unknown_key\".");
  }

  @Test
  void create_SpiExporter_Valid() {
    MetricExporter metricExporter =
        MetricExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .PushMetricExporterModel()
                    .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                spiHelper,
                new ArrayList<>());
    assertThat(metricExporter)
        .isInstanceOf(MetricExporterComponentProvider.TestMetricExporter.class);
    assertThat(
            ((MetricExporterComponentProvider.TestMetricExporter) metricExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
