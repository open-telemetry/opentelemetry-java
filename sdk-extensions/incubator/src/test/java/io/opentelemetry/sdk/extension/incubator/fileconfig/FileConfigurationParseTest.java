/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Aggregation;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOff;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOn;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Console;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogram;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Headers;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric.DefaultHistogramAggregation;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBased;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Prometheus;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Propagator;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Resource;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Selector;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Stream;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBased;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.View;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Zipkin;
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
    OpenTelemetryConfiguration expected = new OpenTelemetryConfiguration();

    expected.withFileFormat("0.1");
    expected.withDisabled(false);

    // General config
    Resource resource =
        new Resource().withAttributes(new Attributes().withServiceName("unknown_service"));
    expected.withResource(resource);

    AttributeLimits attributeLimits =
        new AttributeLimits().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    expected.withAttributeLimits(attributeLimits);

    Propagator propagator =
        new Propagator()
            .withComposite(
                Arrays.asList(
                    "tracecontext", "baggage", "b3", "b3multi", "jaeger", "xray", "ottrace"));
    expected.withPropagator(propagator);

    // TracerProvider config
    TracerProvider tracerProvider = new TracerProvider();

    SpanLimits spanLimits =
        new SpanLimits()
            .withAttributeValueLengthLimit(4096)
            .withAttributeCountLimit(128)
            .withEventCountLimit(128)
            .withLinkCountLimit(128)
            .withEventAttributeCountLimit(128)
            .withLinkAttributeCountLimit(128);
    tracerProvider.withLimits(spanLimits);

    Sampler sampler =
        new Sampler()
            .withParentBased(
                new ParentBased()
                    .withRoot(
                        new Sampler()
                            .withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.0001)))
                    .withRemoteParentSampled(new Sampler().withAlwaysOn(new AlwaysOn()))
                    .withRemoteParentNotSampled(new Sampler().withAlwaysOff(new AlwaysOff()))
                    .withLocalParentSampled(new Sampler().withAlwaysOn(new AlwaysOn()))
                    .withLocalParentNotSampled(new Sampler().withAlwaysOff(new AlwaysOff())));
    tracerProvider.withSampler(sampler);

    SpanProcessor spanProcessor1 =
        new SpanProcessor()
            .withBatch(
                new BatchSpanProcessor()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new SpanExporter()
                            .withOtlp(
                                new Otlp()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers().withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    SpanProcessor spanProcessor2 =
        new SpanProcessor()
            .withBatch(
                new BatchSpanProcessor()
                    .withExporter(
                        new SpanExporter()
                            .withZipkin(
                                new Zipkin()
                                    .withEndpoint("http://localhost:9411/api/v2/spans")
                                    .withTimeout(10_000))));
    SpanProcessor spanProcessor3 =
        new SpanProcessor()
            .withSimple(
                new SimpleSpanProcessor()
                    .withExporter(new SpanExporter().withConsole(new Console())));
    tracerProvider.withProcessors(Arrays.asList(spanProcessor1, spanProcessor2, spanProcessor3));

    expected.withTracerProvider(tracerProvider);
    // end TracerProvider config

    // LoggerProvider config
    LoggerProvider loggerProvider = new LoggerProvider();

    LogRecordLimits logRecordLimits =
        new LogRecordLimits().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    loggerProvider.withLimits(logRecordLimits);

    LogRecordProcessor logRecordProcessor =
        new LogRecordProcessor()
            .withBatch(
                new BatchLogRecordProcessor()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new LogRecordExporter()
                            .withOtlp(
                                new Otlp()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers().withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    loggerProvider.withProcessors(Collections.singletonList(logRecordProcessor));

    expected.withLoggerProvider(loggerProvider);
    // end LoggerProvider config

    // MeterProvider config
    MeterProvider meterProvider = new MeterProvider();

    MetricReader metricReader1 =
        new MetricReader()
            .withPull(
                new PullMetricReader()
                    .withExporter(
                        new MetricExporter()
                            .withPrometheus(
                                new Prometheus().withHost("localhost").withPort(9464))));
    MetricReader metricReader2 =
        new MetricReader()
            .withPeriodic(
                new PeriodicMetricReader()
                    .withInterval(5_000)
                    .withTimeout(30_000)
                    .withExporter(
                        new MetricExporter()
                            .withOtlp(
                                new OtlpMetric()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers().withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withTemporalityPreference("delta")
                                    .withDefaultHistogramAggregation(
                                        DefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM))));
    MetricReader metricReader3 =
        new MetricReader()
            .withPeriodic(
                new PeriodicMetricReader()
                    .withExporter(new MetricExporter().withConsole(new Console())));
    meterProvider.withReaders(Arrays.asList(metricReader1, metricReader2, metricReader3));

    View view =
        new View()
            .withSelector(
                new Selector()
                    .withInstrumentName("my-instrument")
                    .withInstrumentType(Selector.InstrumentType.HISTOGRAM)
                    .withUnit("ms")
                    .withMeterName("my-meter")
                    .withMeterVersion("1.0.0")
                    .withMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0"))
            .withStream(
                new Stream()
                    .withName("new_instrument_name")
                    .withDescription("new_description")
                    .withAggregation(
                        new Aggregation()
                            .withExplicitBucketHistogram(
                                new ExplicitBucketHistogram()
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
      OpenTelemetryConfiguration config = FileConfiguration.parse(configExampleFile);

      // General config
      assertThat(config.getFileFormat()).isEqualTo("0.1");
      assertThat(config.getResource()).isEqualTo(resource);
      assertThat(config.getAttributeLimits()).isEqualTo(attributeLimits);
      assertThat(config.getPropagator()).isEqualTo(propagator);

      // TracerProvider config
      TracerProvider configTracerProvider = config.getTracerProvider();
      assertThat(configTracerProvider.getLimits()).isEqualTo(spanLimits);
      assertThat(configTracerProvider.getSampler()).isEqualTo(sampler);
      assertThat(configTracerProvider.getProcessors())
          .isEqualTo(Arrays.asList(spanProcessor1, spanProcessor2, spanProcessor3));

      // LoggerProvider config
      LoggerProvider configLoggerProvider = config.getLoggerProvider();
      assertThat(configLoggerProvider.getLimits()).isEqualTo(logRecordLimits);
      assertThat(configLoggerProvider.getProcessors())
          .isEqualTo(Collections.singletonList(logRecordProcessor));

      // MeterProvider config
      MeterProvider configMeterProvider = config.getMeterProvider();
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
    OpenTelemetryConfiguration objectPlaceholderModel =
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
    OpenTelemetryConfiguration noObjectPlaceholderModel =
        FileConfiguration.parse(
            new ByteArrayInputStream(noOjbectPlaceholderString.getBytes(StandardCharsets.UTF_8)));

    SpanExporter exporter =
        noObjectPlaceholderModel
            .getTracerProvider()
            .getProcessors()
            .get(0)
            .getBatch()
            .getExporter();
    assertThat(exporter.getConsole()).isNotNull();
    assertThat(exporter.getOtlp()).isNull();

    Aggregation aggregation =
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

    OpenTelemetryConfiguration model =
        FileConfiguration.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    assertThat(model.getFileFormat()).isNull();
    assertThat(model.getDisabled()).isNull();
    assertThat(model.getAttributeLimits().getAttributeValueLengthLimit()).isNull();
    assertThat(model.getTracerProvider().getSampler().getTraceIdRatioBased().getRatio()).isNull();

    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfiguration()
                .withAttributeLimits(new AttributeLimits())
                .withTracerProvider(
                    new TracerProvider()
                        .withSampler(
                            new Sampler().withTraceIdRatioBased(new TraceIdRatioBased()))));
  }

  @ParameterizedTest
  @MethodSource("envVarSubstitutionArgs")
  void envSubstituteAndLoadYaml(String rawYaml, Object expectedYamlResult) {
    Map<String, String> environmentVariables = new HashMap<>();
    environmentVariables.put("STR_1", "value1");
    environmentVariables.put("STR_2", "value2");
    environmentVariables.put("BOOL", "true");
    environmentVariables.put("INT", "1");
    environmentVariables.put("FLOAT", "1.1");

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
        Arguments.of(
            "key1: ${STR_1}\n" + "key2: value2\n",
            mapOf(entry("key1", "value1"), entry("key2", "value2"))),
        Arguments.of(
            "key1: ${STR_1} value1\n" + "key2: value2\n",
            mapOf(entry("key1", "value1 value1"), entry("key2", "value2"))),
        // Multiple environment variables referenced
        Arguments.of("key1: ${STR_1}${STR_2}\n", mapOf(entry("key1", "value1value2"))),
        Arguments.of("key1: ${STR_1} ${STR_2}\n", mapOf(entry("key1", "value1 value2"))),
        // Undefined environment variable
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
            "key1:\n - ${STR_1}\n", mapOf(entry("key1", Collections.singletonList("${STR_1}")))));
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
            + "            endpoint: \"${UNSET_ENV_VAR}\"\n";
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OTEL_EXPORTER_OTLP_ENDPOINT", "http://collector:4317");
    OpenTelemetryConfiguration model =
        FileConfiguration.parse(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), envVars);
    assertThat(model)
        .isEqualTo(
            new OpenTelemetryConfiguration()
                .withFileFormat("0.1")
                .withTracerProvider(
                    new TracerProvider()
                        .withProcessors(
                            Arrays.asList(
                                new SpanProcessor()
                                    .withBatch(
                                        new BatchSpanProcessor()
                                            .withExporter(
                                                new SpanExporter()
                                                    .withOtlp(
                                                        new Otlp()
                                                            .withEndpoint(
                                                                "http://collector:4317")))),
                                new SpanProcessor()
                                    .withBatch(
                                        new BatchSpanProcessor()
                                            .withExporter(
                                                new SpanExporter().withOtlp(new Otlp())))))));
  }
}
