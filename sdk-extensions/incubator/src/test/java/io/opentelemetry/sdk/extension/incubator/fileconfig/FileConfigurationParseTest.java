/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOffModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOnModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributesModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogramModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.HeadersModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetricModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBasedModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PrometheusModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SelectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.StreamModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBasedModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ZipkinModel;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FileConfigurationParseTest {

  @Test
  void parse_BadInputStream() {
    assertThatThrownBy(
            () ->
                FileConfiguration.parseAndCreate(
                    new ByteArrayInputStream("foo".getBytes(StandardCharsets.UTF_8))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unable to parse configuration input stream");
  }

  @Test
  void parse_KitchenSinkExampleFile() throws IOException {
    OpenTelemetryConfigurationModel expected = new OpenTelemetryConfigurationModel();

    expected.withFileFormat("0.1");
    expected.withDisabled(false);

    // General config
    ResourceModel resource =
        new ResourceModel()
            .withAttributes(new AttributesModel().withServiceName("unknown_service"));
    expected.withResource(resource);

    AttributeLimitsModel attributeLimits =
        new AttributeLimitsModel().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    expected.withAttributeLimits(attributeLimits);

    PropagatorModel propagator =
        new PropagatorModel()
            .withComposite(
                Arrays.asList(
                    "tracecontext", "baggage", "b3", "b3multi", "jaeger", "xray", "ottrace"));
    expected.withPropagator(propagator);

    // TracerProvider config
    TracerProviderModel tracerProvider = new TracerProviderModel();

    SpanLimitsModel spanLimits =
        new SpanLimitsModel()
            .withAttributeValueLengthLimit(4096)
            .withAttributeCountLimit(128)
            .withEventCountLimit(128)
            .withLinkCountLimit(128)
            .withEventAttributeCountLimit(128)
            .withLinkAttributeCountLimit(128);
    tracerProvider.withLimits(spanLimits);

    SamplerModel sampler =
        new SamplerModel()
            .withParentBased(
                new ParentBasedModel()
                    .withRoot(
                        new SamplerModel()
                            .withTraceIdRatioBased(new TraceIdRatioBasedModel().withRatio(0.0001)))
                    .withRemoteParentSampled(new SamplerModel().withAlwaysOn(new AlwaysOnModel()))
                    .withRemoteParentNotSampled(
                        new SamplerModel().withAlwaysOff(new AlwaysOffModel()))
                    .withLocalParentSampled(new SamplerModel().withAlwaysOn(new AlwaysOnModel()))
                    .withLocalParentNotSampled(
                        new SamplerModel().withAlwaysOff(new AlwaysOffModel())));
    tracerProvider.withSampler(sampler);

    SpanProcessorModel spanProcessor1 =
        new SpanProcessorModel()
            .withBatch(
                new BatchSpanProcessorModel()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new SpanExporterModel()
                            .withOtlp(
                                new OtlpModel()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new HeadersModel()
                                            .withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    SpanProcessorModel spanProcessor2 =
        new SpanProcessorModel()
            .withBatch(
                new BatchSpanProcessorModel()
                    .withExporter(
                        new SpanExporterModel()
                            .withZipkin(
                                new ZipkinModel()
                                    .withEndpoint("http://localhost:9411/api/v2/spans")
                                    .withTimeout(10_000))));
    SpanProcessorModel spanProcessor3 =
        new SpanProcessorModel()
            .withSimple(
                new SimpleSpanProcessorModel()
                    .withExporter(new SpanExporterModel().withConsole(new ConsoleModel())));
    tracerProvider.withProcessors(Arrays.asList(spanProcessor1, spanProcessor2, spanProcessor3));

    expected.withTracerProvider(tracerProvider);
    // end TracerProvider config

    // LoggerProvider config
    LoggerProviderModel loggerProvider = new LoggerProviderModel();

    LogRecordLimitsModel logRecordLimits =
        new LogRecordLimitsModel().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    loggerProvider.withLimits(logRecordLimits);

    LogRecordProcessorModel logRecordProcessor =
        new LogRecordProcessorModel()
            .withBatch(
                new BatchLogRecordProcessorModel()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new LogRecordExporterModel()
                            .withOtlp(
                                new OtlpModel()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new HeadersModel()
                                            .withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    loggerProvider.withProcessors(Collections.singletonList(logRecordProcessor));

    expected.withLoggerProvider(loggerProvider);
    // end LoggerProvider config

    // MeterProvider config
    MeterProviderModel meterProvider = new MeterProviderModel();

    MetricReaderModel metricReader1 =
        new MetricReaderModel()
            .withPull(
                new PullMetricReaderModel()
                    .withExporter(
                        new MetricExporterModel()
                            .withPrometheus(
                                new PrometheusModel().withHost("localhost").withPort(9464))));
    MetricReaderModel metricReader2 =
        new MetricReaderModel()
            .withPeriodic(
                new PeriodicMetricReaderModel()
                    .withInterval(5_000)
                    .withTimeout(30_000)
                    .withExporter(
                        new MetricExporterModel()
                            .withOtlp(
                                new OtlpMetricModel()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new HeadersModel()
                                            .withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withTemporalityPreference("delta")
                                    .withDefaultHistogramAggregation(
                                        OtlpMetricModel.DefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM))));
    MetricReaderModel metricReader3 =
        new MetricReaderModel()
            .withPeriodic(
                new PeriodicMetricReaderModel()
                    .withExporter(new MetricExporterModel().withConsole(new ConsoleModel())));
    meterProvider.withReaders(Arrays.asList(metricReader1, metricReader2, metricReader3));

    ViewModel view =
        new ViewModel()
            .withSelector(
                new SelectorModel()
                    .withInstrumentName("my-instrument")
                    .withInstrumentType(SelectorModel.InstrumentType.HISTOGRAM)
                    .withUnit("ms")
                    .withMeterName("my-meter")
                    .withMeterVersion("1.0.0")
                    .withMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0"))
            .withStream(
                new StreamModel()
                    .withName("new_instrument_name")
                    .withDescription("new_description")
                    .withAggregation(
                        new AggregationModel()
                            .withExplicitBucketHistogram(
                                new ExplicitBucketHistogramModel()
                                    .withBoundaries(
                                        Arrays.asList(
                                            0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0,
                                            750.0, 1000.0, 2500.0, 5000.0, 7500.0, 10000.0))
                                    .withRecordMinMax(true)))
                    .withAttributeKeys(Arrays.asList("key1", "key2")));
    meterProvider.withViews(Collections.singletonList(view));

    expected.withMeterProvider(meterProvider);
    // end MeterProvider config

    try (FileInputStream configExampleFile =
        new FileInputStream(System.getenv("CONFIG_EXAMPLE_DIR") + "/kitchen-sink.yaml")) {
      OpenTelemetryConfigurationModel config = FileConfiguration.parse(configExampleFile);

      // General config
      assertThat(config.getFileFormat()).isEqualTo("0.1");
      assertThat(config.getResource()).isEqualTo(resource);
      assertThat(config.getAttributeLimits()).isEqualTo(attributeLimits);
      assertThat(config.getPropagator()).isEqualTo(propagator);

      // TracerProvider config
      TracerProviderModel configTracerProvider = config.getTracerProvider();
      assertThat(configTracerProvider.getLimits()).isEqualTo(spanLimits);
      assertThat(configTracerProvider.getSampler()).isEqualTo(sampler);
      assertThat(configTracerProvider.getProcessors())
          .isEqualTo(Arrays.asList(spanProcessor1, spanProcessor2, spanProcessor3));

      // LoggerProvider config
      LoggerProviderModel configLoggerProvider = config.getLoggerProvider();
      assertThat(configLoggerProvider.getLimits()).isEqualTo(logRecordLimits);
      assertThat(configLoggerProvider.getProcessors())
          .isEqualTo(Collections.singletonList(logRecordProcessor));

      // MeterProvider config
      MeterProviderModel configMeterProvider = config.getMeterProvider();
      assertThat(configMeterProvider.getReaders())
          .isEqualTo(Arrays.asList(metricReader1, metricReader2, metricReader3));
      assertThat(configMeterProvider.getViews()).isEqualTo(Collections.singletonList(view));

      // All configuration
      assertThat(config).isEqualTo(expected);
    }
  }

  @Test
  void parse_nullValuesParsedToEmptyObjects() {
    String objectPlaceholderString =
        "file_format: \"0.1\"\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          console: {}\n"
            + "meter_provider:\n"
            + "  views:\n"
            + "    - selector:\n"
            + "        instrument_type: histogram\n"
            + "      stream:\n"
            + "        aggregation:\n"
            + "          drop: {}\n";
    OpenTelemetryConfigurationModel objectPlaceholderModel =
        FileConfiguration.parse(
            new ByteArrayInputStream(objectPlaceholderString.getBytes(StandardCharsets.UTF_8)));

    String noOjbectPlaceholderString =
        "file_format: \"0.1\"\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          console:\n"
            + "meter_provider:\n"
            + "  views:\n"
            + "    - selector:\n"
            + "        instrument_type: histogram\n"
            + "      stream:\n"
            + "        aggregation:\n"
            + "          drop:\n";
    OpenTelemetryConfigurationModel noObjectPlaceholderModel =
        FileConfiguration.parse(
            new ByteArrayInputStream(noOjbectPlaceholderString.getBytes(StandardCharsets.UTF_8)));

    SpanExporterModel exporter =
        noObjectPlaceholderModel
            .getTracerProvider()
            .getProcessors()
            .get(0)
            .getBatch()
            .getExporter();
    assertThat(exporter.getConsole()).isNotNull();
    assertThat(exporter.getOtlp()).isNull();

    AggregationModel aggregation =
        noObjectPlaceholderModel.getMeterProvider().getViews().get(0).getStream().getAggregation();
    assertThat(aggregation.getDrop()).isNotNull();
    assertThat(aggregation.getSum()).isNull();

    assertThat(objectPlaceholderModel).isEqualTo(noObjectPlaceholderModel);
  }

  @Test
  void parse_nullBoxedPrimitivesParsedToNull() {
    String yaml =
        "file_format:\n" // String
            + "disabled:\n" // Boolean
            + "attribute_limits:\n"
            + "  attribute_value_length_limit:\n" // Integer
            + "tracer_provider:\n"
            + "  sampler:\n"
            + "    trace_id_ratio_based:\n"
            + "      ratio:\n"; // Double

    OpenTelemetryConfigurationModel model =
        FileConfiguration.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    assertThat(model.getFileFormat()).isNull();
    assertThat(model.getDisabled()).isNull();
    assertThat(model.getAttributeLimits().getAttributeValueLengthLimit()).isNull();
    assertThat(model.getTracerProvider().getSampler().getTraceIdRatioBased().getRatio()).isNull();

    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfigurationModel()
                .withAttributeLimits(new AttributeLimitsModel())
                .withTracerProvider(
                    new TracerProviderModel()
                        .withSampler(
                            new SamplerModel()
                                .withTraceIdRatioBased(new TraceIdRatioBasedModel()))));
  }

  @ParameterizedTest
  @MethodSource("coreSchemaValuesArgs")
  void coreSchemaValues(String rawYaml, Object expectedYamlResult) {
    Object yaml =
        FileConfiguration.loadYaml(
            new ByteArrayInputStream(rawYaml.getBytes(StandardCharsets.UTF_8)),
            Collections.emptyMap());
    assertThat(yaml).isEqualTo(expectedYamlResult);
  }

  @SuppressWarnings("unchecked")
  private static java.util.stream.Stream<Arguments> coreSchemaValuesArgs() {
    return java.util.stream.Stream.of(
        Arguments.of("key1: 0o123\n", mapOf(entry("key1", 83))),
        Arguments.of("key1: 0123\n", mapOf(entry("key1", 123))),
        Arguments.of("key1: 0xdeadbeef\n", mapOf(entry("key1", 3735928559L))),
        Arguments.of("key1: \"0xdeadbeef\"\n", mapOf(entry("key1", "0xdeadbeef"))));
  }

  @ParameterizedTest
  @MethodSource("envVarSubstitutionArgs")
  void envSubstituteAndLoadYaml(String rawYaml, Object expectedYamlResult) {
    Map<String, String> environmentVariables = new HashMap<>();
    environmentVariables.put("STR_1", "value1");
    environmentVariables.put("STR_2", "value2");
    environmentVariables.put("EMPTY_STR", "");
    environmentVariables.put("BOOL", "true");
    environmentVariables.put("INT", "1");
    environmentVariables.put("FLOAT", "1.1");
    environmentVariables.put("HEX", "0xdeadbeef");

    Object yaml =
        FileConfiguration.loadYaml(
            new ByteArrayInputStream(rawYaml.getBytes(StandardCharsets.UTF_8)),
            environmentVariables);
    assertThat(yaml).isEqualTo(expectedYamlResult);
  }

  @SuppressWarnings("unchecked")
  private static java.util.stream.Stream<Arguments> envVarSubstitutionArgs() {
    return java.util.stream.Stream.of(
        // Simple cases
        Arguments.of("key1: ${STR_1}\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: ${BOOL}\n", mapOf(entry("key1", true))),
        Arguments.of("key1: ${INT}\n", mapOf(entry("key1", 1))),
        Arguments.of("key1: ${FLOAT}\n", mapOf(entry("key1", 1.1))),
        Arguments.of("key1: ${HEX}\n", mapOf(entry("key1", 3735928559L))),
        Arguments.of(
            "key1: ${STR_1}\n" + "key2: value2\n",
            mapOf(entry("key1", "value1"), entry("key2", "value2"))),
        Arguments.of(
            "key1: ${STR_1} value1\n" + "key2: value2\n",
            mapOf(entry("key1", "value1 value1"), entry("key2", "value2"))),
        // Multiple environment variables referenced
        Arguments.of("key1: ${STR_1}${STR_2}\n", mapOf(entry("key1", "value1value2"))),
        Arguments.of("key1: ${STR_1} ${STR_2}\n", mapOf(entry("key1", "value1 value2"))),
        // Undefined / empty environment variable
        Arguments.of("key1: ${EMPTY_STR}\n", mapOf(entry("key1", null))),
        Arguments.of("key1: ${STR_3}\n", mapOf(entry("key1", null))),
        Arguments.of("key1: ${STR_1} ${STR_3}\n", mapOf(entry("key1", "value1"))),
        // Environment variable keys must match pattern: [a-zA-Z_]+[a-zA-Z0-9_]*
        Arguments.of("key1: ${VAR&}\n", mapOf(entry("key1", "${VAR&}"))),
        // Environment variable substitution only takes place in scalar values of maps
        Arguments.of("${STR_1}: value1\n", mapOf(entry("${STR_1}", "value1"))),
        Arguments.of(
            "key1:\n  ${STR_1}: value1\n",
            mapOf(entry("key1", mapOf(entry("${STR_1}", "value1"))))),
        Arguments.of(
            "key1:\n - ${STR_1}\n", mapOf(entry("key1", Collections.singletonList("${STR_1}")))),
        // Quoted environment variables
        Arguments.of("key1: \"${HEX}\"\n", mapOf(entry("key1", "0xdeadbeef"))),
        Arguments.of("key1: \"${STR_1}\"\n", mapOf(entry("key1", "value1"))),
        Arguments.of("key1: \"${EMPTY_STR}\"\n", mapOf(entry("key1", ""))),
        Arguments.of("key1: \"${BOOL}\"\n", mapOf(entry("key1", "true"))),
        Arguments.of("key1: \"${INT}\"\n", mapOf(entry("key1", "1"))),
        Arguments.of("key1: \"${FLOAT}\"\n", mapOf(entry("key1", "1.1"))));
  }

  private static <K, V> Map.Entry<K, V> entry(K key, @Nullable V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> mapOf(Map.Entry<String, ?>... entries) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, ?> entry : entries) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  @Test
  void read_WithEnvironmentVariables() {
    String yaml =
        "file_format: \"0.1\"\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp:\n"
            + "            endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT}\n"
            + "    - batch:\n"
            + "        exporter:\n"
            + "          otlp:\n"
            + "            endpoint: ${UNSET_ENV_VAR}\n";
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OTEL_EXPORTER_OTLP_ENDPOINT", "http://collector:4317");
    OpenTelemetryConfigurationModel model =
        FileConfiguration.parse(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), envVars);
    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfigurationModel()
                .withFileFormat("0.1")
                .withTracerProvider(
                    new TracerProviderModel()
                        .withProcessors(
                            Arrays.asList(
                                new SpanProcessorModel()
                                    .withBatch(
                                        new BatchSpanProcessorModel()
                                            .withExporter(
                                                new SpanExporterModel()
                                                    .withOtlp(
                                                        new OtlpModel()
                                                            .withEndpoint(
                                                                "http://collector:4317")))),
                                new SpanProcessorModel()
                                    .withBatch(
                                        new BatchSpanProcessorModel()
                                            .withExporter(
                                                new SpanExporterModel()
                                                    .withOtlp(new OtlpModel())))))));
  }
}
