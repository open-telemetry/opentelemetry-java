/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.MetricExporterComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalOtlpFileMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.NameStringValuePairModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class MetricExporterFactoryTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private CapturingComponentLoader capturingComponentLoader;
  private SpiHelper spiHelper;
  private DeclarativeConfigContext context;

  @BeforeEach
  void setup() {
    capturingComponentLoader = new CapturingComponentLoader();
    spiHelper = SpiHelper.create(capturingComponentLoader);
    context = new DeclarativeConfigContext(spiHelper);
  }

  @Test
  void create_OtlpHttpDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpMetricExporter expectedExporter =
        OtlpHttpMetricExporter.getDefault().toBuilder()
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
            .build();
    cleanup.addCloseable(expectedExporter);

    MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel().withOtlpHttp(new OtlpHttpMetricExporterModel()),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_http");
    assertThat(configProperties).isNotNull();
    assertThat(configProperties.getString("protocol")).isNull();
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
  void create_OtlpHttpConfigured(@TempDir Path tempDir)
      throws CertificateEncodingException, IOException {
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
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
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
                new PushMetricExporterModel()
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
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_http");
    assertThat(configProperties).isNotNull();
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://example:4318/v1/metrics");
    List<DeclarativeConfigProperties> headers = configProperties.getStructuredList("headers");
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
  void create_OtlpGrpcDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpGrpcMetricExporter expectedExporter =
        OtlpGrpcMetricExporter.getDefault().toBuilder()
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
            .build();
    cleanup.addCloseable(expectedExporter);

    MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel().withOtlpGrpc(new OtlpGrpcMetricExporterModel()),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_grpc");
    assertThat(configProperties).isNotNull();
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
  void create_OtlpGrpcConfigured(@TempDir Path tempDir)
      throws CertificateEncodingException, IOException {
    List<Closeable> closeables = new ArrayList<>();
    OtlpGrpcMetricExporter expectedExporter =
        OtlpGrpcMetricExporter.builder()
            .setEndpoint("http://example:4317")
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .setTimeout(Duration.ofSeconds(15))
            .setCompression("gzip")
            .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
            .setDefaultAggregationSelector(
                DefaultAggregationSelector.getDefault()
                    .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()))
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
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
                new PushMetricExporterModel()
                    .withOtlpGrpc(
                        new OtlpGrpcMetricExporterModel()
                            .withEndpoint("http://example:4317")
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
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_grpc");
    assertThat(configProperties).isNotNull();
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://example:4317");
    List<DeclarativeConfigProperties> headers = configProperties.getStructuredList("headers");
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
    List<Closeable> closeables = new ArrayList<>();
    LoggingMetricExporter expectedExporter = LoggingMetricExporter.create();
    cleanup.addCloseable(expectedExporter);

    io.opentelemetry.sdk.metrics.export.MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(new PushMetricExporterModel().withConsole(new ConsoleExporterModel()), context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

  @Test
  void create_OtlpFile() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpStdoutMetricExporter expectedExporter = OtlpStdoutMetricExporter.builder().build();
    cleanup.addCloseable(expectedExporter);

    MetricExporter exporter =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel()
                    .withOtlpFileDevelopment(new ExperimentalOtlpFileMetricExporterModel()),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_file/development");
    assertThat(configProperties).isNotNull();
  }

  @Test
  void create_SpiExporter_Unknown() {
    assertThatThrownBy(
            () ->
                MetricExporterFactory.getInstance()
                    .create(
                        new PushMetricExporterModel()
                            .withAdditionalProperty(
                                "unknown_key", ImmutableMap.of("key1", "value1")),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.metrics.export.MetricExporter with name \"unknown_key\".");
  }

  @Test
  void create_SpiExporter_Valid() {
    MetricExporter metricExporter =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel()
                    .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                context);
    assertThat(metricExporter)
        .isInstanceOf(MetricExporterComponentProvider.TestMetricExporter.class);
    assertThat(
            ((MetricExporterComponentProvider.TestMetricExporter) metricExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
