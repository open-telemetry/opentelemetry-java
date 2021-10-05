/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_METRICS;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_TRACES;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class OtlpConfigUtilTest {

  private static final String GENERIC_ENDPOINT_KEY = "otel.exporter.otlp.endpoint";
  private static final String TRACES_ENDPOINT_KEY = "otel.exporter.otlp.traces.endpoint";
  private static final String METRICS_ENDPOINT_KEY = "otel.exporter.otlp.metrics.endpoint";

  @Test
  void getOtlpProtocolDefault() {
    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES, DefaultConfigProperties.createForTest(Collections.emptyMap())))
        .isEqualTo(PROTOCOL_GRPC);

    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES,
                DefaultConfigProperties.createForTest(
                    ImmutableMap.of("otel.experimental.exporter.otlp.protocol", "foo"))))
        .isEqualTo("foo");

    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES,
                DefaultConfigProperties.createForTest(
                    ImmutableMap.of(
                        "otel.experimental.exporter.otlp.protocol", "foo",
                        "otel.experimental.exporter.otlp.traces.protocol", "bar"))))
        .isEqualTo("bar");

    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES,
                DefaultConfigProperties.createForTest(
                    ImmutableMap.of(
                        "otel.experimental.exporter.otlp.protocol", "foo",
                        "otel.experimental.exporter.otlp.traces.protocol", "bar",
                        "otel.exporter.otlp.protocol", "baz"))))
        .isEqualTo("baz");

    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES,
                DefaultConfigProperties.createForTest(
                    ImmutableMap.of(
                        "otel.experimental.exporter.otlp.protocol", "foo",
                        "otel.experimental.exporter.otlp.traces.protocol", "bar",
                        "otel.exporter.otlp.protocol", "baz",
                        "otel.exporter.otlp.traces.protocol", "qux"))))
        .isEqualTo("qux");
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
                    "http://localhost:4317/path",
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

  private static ThrowingCallable configureEndpointCallable(Map<String, String> properties) {
    return () -> configureEndpoint(DATA_TYPE_TRACES, properties);
  }

  @Test
  void configureOtlpExporterBuilder_HttpGenericEndpointKey() {
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4317/"))
        .isEqualTo("http://localhost:4317/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4317/foo"))
        .isEqualTo("http://localhost:4317/foo/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4317/foo/"))
        .isEqualTo("http://localhost:4317/foo/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4317/v1/traces"))
        .isEqualTo("http://localhost:4317/v1/traces/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, GENERIC_ENDPOINT_KEY, "http://localhost:4317/v1/metrics"))
        .isEqualTo("http://localhost:4317/v1/metrics/v1/traces");

    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4317/"))
        .isEqualTo("http://localhost:4317/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4317/foo"))
        .isEqualTo("http://localhost:4317/foo/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4317/foo/"))
        .isEqualTo("http://localhost:4317/foo/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4317/v1/metrics"))
        .isEqualTo("http://localhost:4317/v1/metrics/v1/metrics");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, GENERIC_ENDPOINT_KEY, "http://localhost:4317/v1/traces"))
        .isEqualTo("http://localhost:4317/v1/traces/v1/metrics");
  }

  @Test
  void configureOtlpExporterBuilder_HttpTracesEndpointKey() {
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4317/"))
        .isEqualTo("http://localhost:4317/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4317/v1/traces"))
        .isEqualTo("http://localhost:4317/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_TRACES, TRACES_ENDPOINT_KEY, "http://localhost:4317/foo/bar"))
        .isEqualTo("http://localhost:4317/foo/bar");
    assertThat(
            configureEndpoint(
                DATA_TYPE_TRACES,
                ImmutableMap.of(
                    "otel.exporter.otlp.protocol",
                    PROTOCOL_HTTP_PROTOBUF,
                    GENERIC_ENDPOINT_KEY,
                    "http://localhost:4317/foo/bar",
                    TRACES_ENDPOINT_KEY,
                    "http://localhost:4317/baz/qux")))
        .isEqualTo("http://localhost:4317/baz/qux");
  }

  @Test
  void configureOtlpExporterBuilder_HttpMetricsEndpointKey() {
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4317/"))
        .isEqualTo("http://localhost:4317/");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4317/v1/traces"))
        .isEqualTo("http://localhost:4317/v1/traces");
    assertThat(
            configureEndpointForHttp(
                DATA_TYPE_METRICS, METRICS_ENDPOINT_KEY, "http://localhost:4317/foo/bar"))
        .isEqualTo("http://localhost:4317/foo/bar");
    assertThat(
            configureEndpoint(
                DATA_TYPE_METRICS,
                ImmutableMap.of(
                    "otel.exporter.otlp.protocol",
                    PROTOCOL_HTTP_PROTOBUF,
                    GENERIC_ENDPOINT_KEY,
                    "http://localhost:4317/foo/bar",
                    METRICS_ENDPOINT_KEY,
                    "http://localhost:4317/baz/qux")))
        .isEqualTo("http://localhost:4317/baz/qux");
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
        DefaultConfigProperties.createForTest(properties),
        endpoint::set,
        (value1, value2) -> {},
        value -> {},
        value -> {},
        value -> {});

    return endpoint.get();
  }
}
