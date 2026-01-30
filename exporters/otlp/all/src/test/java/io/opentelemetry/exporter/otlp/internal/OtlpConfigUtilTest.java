/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_LOGS;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_METRICS;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.DATA_TYPE_TRACES;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.LoggingEvent;

class OtlpConfigUtilTest {

  @RegisterExtension
  final LogCapturer logs = LogCapturer.create().captureForType(OtlpConfigUtil.class);

  private static final String GENERIC_ENDPOINT_KEY = "otel.exporter.otlp.endpoint";
  private static final String TRACES_ENDPOINT_KEY = "otel.exporter.otlp.traces.endpoint";
  private static final String METRICS_ENDPOINT_KEY = "otel.exporter.otlp.metrics.endpoint";
  private static final String LOGS_ENDPOINT_KEY = "otel.exporter.otlp.logs.endpoint";

  @Test
  void getOtlpProtocolDefault() {
    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES, DefaultConfigProperties.createFromMap(Collections.emptyMap())))
        .isEqualTo(PROTOCOL_GRPC);

    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES,
                DefaultConfigProperties.createFromMap(
                    ImmutableMap.of("otel.exporter.otlp.protocol", "foo"))))
        .isEqualTo("foo");

    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES,
                DefaultConfigProperties.createFromMap(
                    ImmutableMap.of(
                        "otel.exporter.otlp.protocol", "foo",
                        "otel.exporter.otlp.traces.protocol", "bar"))))
        .isEqualTo("bar");
  }

  @Test
  void configureOtlpExporterBuilder_ValidEndpoints() {
    assertThatCode(
            configureEndpointCallable(
                ImmutableMap.of(GENERIC_ENDPOINT_KEY, "http://localhost:4317")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpointCallable(
                ImmutableMap.of(GENERIC_ENDPOINT_KEY, "http://localhost:4317/")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpointCallable(ImmutableMap.of(GENERIC_ENDPOINT_KEY, "http://localhost")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpointCallable(ImmutableMap.of(GENERIC_ENDPOINT_KEY, "https://localhost")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpointCallable(
                ImmutableMap.of(GENERIC_ENDPOINT_KEY, "http://foo:bar@localhost")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpointCallable(
                ImmutableMap.of(
                    GENERIC_ENDPOINT_KEY,
                    "http://localhost:4318/path",
                    "otel.exporter.otlp.protocol",
                    "http/protobuf")))
        .doesNotThrowAnyException();
  }

  @Test
  void configureOtlpExporterBuilder_InvalidEndpoints() {
    assertThatThrownBy(configureEndpointCallable(ImmutableMap.of(GENERIC_ENDPOINT_KEY, "/foo/bar")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must be a valid URL:");
    assertThatThrownBy(
            configureEndpointCallable(
                ImmutableMap.of(GENERIC_ENDPOINT_KEY, "file://localhost:4317")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint scheme must be http or https:");
    assertThatThrownBy(
            configureEndpointCallable(
                ImmutableMap.of(GENERIC_ENDPOINT_KEY, "http://localhost:4317?foo=bar")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must not have a query string:");
    assertThatThrownBy(
            configureEndpointCallable(
                ImmutableMap.of(GENERIC_ENDPOINT_KEY, "http://localhost:4317#fragment")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must not have a fragment:");
    assertThatThrownBy(
            configureEndpointCallable(
                ImmutableMap.of(
                    GENERIC_ENDPOINT_KEY,
                    "http://localhost:4317/path",
                    "otel.exporter.otlp.protocol",
                    "grpc")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must not have a path:");
  }

  @SuppressLogger(OtlpConfigUtil.class)
  @ParameterizedTest
  @MethodSource("misalignedOtlpPortArgs")
  void configureOtlpExporterBuilder_MisalignedOtlpPort(
      String protocol, String endpoint, @Nullable String expectedLog) {
    configureEndpoint(
        DATA_TYPE_TRACES,
        ImmutableMap.of(GENERIC_ENDPOINT_KEY, endpoint, "otel.exporter.otlp.protocol", protocol));

    List<String> logMessages =
        logs.getEvents().stream().map(LoggingEvent::getMessage).collect(toList());
    if (expectedLog == null) {
      assertThat(logMessages).isEmpty();
    } else {
      assertThat(logMessages).contains(expectedLog);
    }
  }

  private static Stream<Arguments> misalignedOtlpPortArgs() {
    return Stream.of(
        Arguments.of("http/protobuf", "http://localhost:4318/path", null),
        Arguments.of("http/protobuf", "http://localhost:8080/path", null),
        Arguments.of("http/protobuf", "http://localhost/path", null),
        Arguments.of(
            "http/protobuf",
            "http://localhost:4317/path",
            "OTLP exporter endpoint port is likely incorrect for protocol version \"http/protobuf\". The endpoint http://localhost:4317/path has port 4317. Typically, the \"http/protobuf\" version of OTLP uses port 4318."),
        Arguments.of("grpc", "http://localhost:4317/", null),
        Arguments.of("grpc", "http://localhost:8080/", null),
        Arguments.of("grpc", "http://localhost/", null),
        Arguments.of(
            "grpc",
            "http://localhost:4318/",
            "OTLP exporter endpoint port is likely incorrect for protocol version \"grpc\". The endpoint http://localhost:4318/ has port 4318. Typically, the \"grpc\" version of OTLP uses port 4317."));
  }

  private static ThrowingCallable configureEndpointCallable(Map<String, String> properties) {
    return () -> configureEndpoint(DATA_TYPE_TRACES, properties);
  }

  @Test
  void configureOtlpExporterBuilder_HttpGenericEndpointKey() {
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4318/"))
        .isEqualTo("http://localhost:4318/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4318/foo"))
        .isEqualTo("http://localhost:4318/foo/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4318/foo/"))
        .isEqualTo("http://localhost:4318/foo/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4318/v1/traces"))
        .isEqualTo("http://localhost:4318/v1/traces/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4318/v1/metrics"))
        .isEqualTo("http://localhost:4318/v1/metrics/v1/traces");

    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/"))
        .isEqualTo("http://localhost:4318/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/foo"))
        .isEqualTo("http://localhost:4318/foo/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/foo/"))
        .isEqualTo("http://localhost:4318/foo/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/v1/metrics"))
        .isEqualTo("http://localhost:4318/v1/metrics/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/v1/traces"))
        .isEqualTo("http://localhost:4318/v1/traces/v1/metrics");

    assertThat(
            configureEndpointForHttp(DATA_TYPE_LOGS, GENERIC_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/v1/logs");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_LOGS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/"))
        .isEqualTo("http://localhost:4318/v1/logs");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_LOGS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/foo"))
        .isEqualTo("http://localhost:4318/foo/v1/logs");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_LOGS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/foo/"))
        .isEqualTo("http://localhost:4318/foo/v1/logs");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_LOGS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/v1/logs"))
        .isEqualTo("http://localhost:4318/v1/logs/v1/logs");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_LOGS, GENERIC_ENDPOINT_KEY, "http://localhost:4318/v1/traces"))
        .isEqualTo("http://localhost:4318/v1/traces/v1/logs");
  }

  @Test
  void configureOtlpExporterBuilder_HttpTracesEndpointKey() {
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4318/"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4318/v1/traces"))
        .isEqualTo("http://localhost:4318/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4318/foo/bar"))
        .isEqualTo("http://localhost:4318/foo/bar");
    assertThat(
            configureEndpoint(
                DATA_TYPE_TRACES,
                ImmutableMap.of(
                    "otel.exporter.otlp.protocol",
                    PROTOCOL_HTTP_PROTOBUF,
                    GENERIC_ENDPOINT_KEY,
                    "http://localhost:4318/foo/bar",
                    TRACES_ENDPOINT_KEY,
                    "http://localhost:4318/baz/qux")))
        .isEqualTo("http://localhost:4318/baz/qux");
  }

  @Test
  void configureOtlpExporterBuilder_HttpMetricsEndpointKey() {
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4318/"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4318/v1/traces"))
        .isEqualTo("http://localhost:4318/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4318/foo/bar"))
        .isEqualTo("http://localhost:4318/foo/bar");
    assertThat(
            configureEndpoint(
                DATA_TYPE_METRICS,
                ImmutableMap.of(
                    "otel.exporter.otlp.protocol",
                    PROTOCOL_HTTP_PROTOBUF,
                    GENERIC_ENDPOINT_KEY,
                    "http://localhost:4318/foo/bar",
                    METRICS_ENDPOINT_KEY,
                    "http://localhost:4318/baz/qux")))
        .isEqualTo("http://localhost:4318/baz/qux");
  }

  @Test
  void configureOtlpExporterBuilder_HttpLogsEndpointKey() {
    assertThat(configureEndpointForHttp(DATA_TYPE_LOGS, LOGS_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/");
    assertThat(configureEndpointForHttp(DATA_TYPE_LOGS, LOGS_ENDPOINT_KEY, "http://localhost:4318"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(DATA_TYPE_LOGS, LOGS_ENDPOINT_KEY, "http://localhost:4318/"))
        .isEqualTo("http://localhost:4318/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_LOGS, LOGS_ENDPOINT_KEY, "http://localhost:4318/v1/logs"))
        .isEqualTo("http://localhost:4318/v1/logs");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_LOGS, LOGS_ENDPOINT_KEY, "http://localhost:4318/foo/bar"))
        .isEqualTo("http://localhost:4318/foo/bar");
    assertThat(
            configureEndpoint(
                DATA_TYPE_LOGS,
                ImmutableMap.of(
                    "otel.exporter.otlp.protocol",
                    PROTOCOL_HTTP_PROTOBUF,
                    GENERIC_ENDPOINT_KEY,
                    "http://localhost:4318/foo/bar",
                    LOGS_ENDPOINT_KEY,
                    "http://localhost:4318/baz/qux")))
        .isEqualTo("http://localhost:4318/baz/qux");
  }

  private static String configureEndpointForHttp(
      String dataType, String endpointPropertyKey, String endpointPropertyValue) {
    return configureEndpoint(
        dataType,
        ImmutableMap.of(
            "otel.exporter.otlp.protocol",
            PROTOCOL_HTTP_PROTOBUF,
            endpointPropertyKey,
            endpointPropertyValue));
  }

  /** Configure and return the endpoint for the data type using the given properties. */
  private static String configureEndpoint(String dataType, Map<String, String> properties) {
    AtomicReference<String> endpoint = new AtomicReference<>("");

    OtlpConfigUtil.configureOtlpExporterBuilder(
        dataType,
        DefaultConfigProperties.createFromMap(properties),
        value -> {},
        endpoint::set,
        (value1, value2) -> {},
        value -> {},
        value -> {},
        value -> {},
        (value1, value2) -> {},
        value -> {},
        value -> {},
        value -> {});

    return endpoint.get();
  }

  @Test
  void configureOtlpAggregationTemporality() {
    assertThatThrownBy(
            () ->
                configureAggregationTemporality(
                    ImmutableMap.of("otel.exporter.otlp.metrics.temporality.preference", "foo")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized aggregation temporality:");

    assertThat(
            configureAggregationTemporality(
                ImmutableMap.of("otel.exporter.otlp.metrics.temporality.preference", "CUMULATIVE")))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            configureAggregationTemporality(
                ImmutableMap.of("otel.exporter.otlp.metrics.temporality.preference", "cumulative")))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            configureAggregationTemporality(
                ImmutableMap.of("otel.exporter.otlp.metrics.temporality.preference", "DELTA")))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            configureAggregationTemporality(
                ImmutableMap.of("otel.exporter.otlp.metrics.temporality.preference", "delta")))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  /** Configure and return the aggregation temporality using the given properties. */
  private static AggregationTemporality configureAggregationTemporality(
      Map<String, String> properties) {
    AtomicReference<AggregationTemporalitySelector> temporalityRef = new AtomicReference<>();
    ConfigProperties config = DefaultConfigProperties.createFromMap(properties);
    ExporterBuilderUtil.configureOtlpAggregationTemporality(config, temporalityRef::set);
    // We apply the temporality selector to a HISTOGRAM instrument to simplify assertions
    return temporalityRef.get().getAggregationTemporality(InstrumentType.HISTOGRAM);
  }

  @Test
  void configureOtlpHistogramDefaultAggregation() {
    assertThatThrownBy(
            () ->
                configureHistogramDefaultAggregation(
                    ImmutableMap.of(
                        "otel.exporter.otlp.metrics.default.histogram.aggregation", "foo")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized default histogram aggregation:");
    assertThat(
            configureHistogramDefaultAggregation(
                    ImmutableMap.of(
                        "otel.exporter.otlp.metrics.default.histogram.aggregation",
                        "base2_exponential_bucket_histogram"))
                .getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
    assertThat(
            configureHistogramDefaultAggregation(
                    ImmutableMap.of(
                        "otel.exporter.otlp.metrics.default.histogram.aggregation",
                        "BASE2_EXPONENTIAL_BUCKET_HISTOGRAM"))
                .getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());

    assertThat(
            configureHistogramDefaultAggregation(
                ImmutableMap.of(
                    "otel.exporter.otlp.metrics.default.histogram.aggregation",
                    "explicit_bucket_histogram")))
        .isNull();
    assertThat(
            configureHistogramDefaultAggregation(
                ImmutableMap.of(
                    "otel.exporter.otlp.metrics.default.histogram.aggregation",
                    "EXPLICIT_BUCKET_HISTOGRAM")))
        .isNull();
  }

  private static DefaultAggregationSelector configureHistogramDefaultAggregation(
      Map<String, String> properties) {
    AtomicReference<DefaultAggregationSelector> aggregationRef = new AtomicReference<>();
    ConfigProperties config = DefaultConfigProperties.createFromMap(properties);
    ExporterBuilderUtil.configureOtlpHistogramDefaultAggregation(config, aggregationRef::set);
    // We apply the temporality selector to a HISTOGRAM instrument to simplify assertions
    return aggregationRef.get();
  }
}
