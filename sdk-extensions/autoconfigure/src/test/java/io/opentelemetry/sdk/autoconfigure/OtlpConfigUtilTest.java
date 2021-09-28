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
            configureEndpoint(
                ImmutableMap.of("otel.exporter.otlp.endpoint", "http://localhost:4317")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpoint(
                ImmutableMap.of("otel.exporter.otlp.endpoint", "http://localhost:4317/")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpoint(ImmutableMap.of("otel.exporter.otlp.endpoint", "http://localhost")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpoint(ImmutableMap.of("otel.exporter.otlp.endpoint", "https://localhost")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpoint(
                ImmutableMap.of("otel.exporter.otlp.endpoint", "http://foo:bar@localhost")))
        .doesNotThrowAnyException();
    assertThatCode(
            configureEndpoint(
                ImmutableMap.of(
                    "otel.exporter.otlp.endpoint", "http://localhost:4317/path",
                    "otel.exporter.otlp.protocol", "http/protobuf")))
        .doesNotThrowAnyException();
  }

  @Test
  void configureOtlpExporterBuilder_InvalidEndpoints() {
    assertThatThrownBy(
            configureEndpoint(ImmutableMap.of("otel.exporter.otlp.endpoint", "/foo/bar")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must be a valid URL:");
    assertThatThrownBy(
            configureEndpoint(
                ImmutableMap.of("otel.exporter.otlp.endpoint", "file://localhost:4317")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint scheme must be http or https:");
    assertThatThrownBy(
            configureEndpoint(
                ImmutableMap.of("otel.exporter.otlp.endpoint", "http://localhost:4317?foo=bar")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must not have a query string:");
    assertThatThrownBy(
            configureEndpoint(
                ImmutableMap.of("otel.exporter.otlp.endpoint", "http://localhost:4317#fragment")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must not have a fragment:");
    assertThatThrownBy(
            configureEndpoint(
                ImmutableMap.of(
                    "otel.exporter.otlp.endpoint", "http://localhost:4317/path",
                    "otel.exporter.otlp.protocol", "grpc")))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("OTLP endpoint must not have a path:");
  }

  private static ThrowingCallable configureEndpoint(Map<String, String> properties) {
    return () ->
        OtlpConfigUtil.configureOtlpExporterBuilder(
            DATA_TYPE_TRACES,
            DefaultConfigProperties.createForTest(properties),
            value -> {},
            (value1, value2) -> {},
            value -> {},
            value -> {},
            value -> {});
  }

  @Test
  void configureOtlpExporterBuilder_HttpProtobufEndpoint() {
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_TRACES, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/v1/traces");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_TRACES, "http://localhost:4317/"))
        .isEqualTo("http://localhost:4317/v1/traces");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_TRACES, "http://localhost:4317/foo"))
        .isEqualTo("http://localhost:4317/foo/v1/traces");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_TRACES, "http://localhost:4317/foo/"))
        .isEqualTo("http://localhost:4317/foo/v1/traces");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_TRACES, "http://localhost:4317/v1/traces"))
        .isEqualTo("http://localhost:4317/v1/traces");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_TRACES, "http://localhost:4317/v1/metrics"))
        .isEqualTo("http://localhost:4317/v1/metrics/v1/traces");

    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_METRICS, "http://localhost:4317"))
        .isEqualTo("http://localhost:4317/v1/metrics");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_METRICS, "http://localhost:4317/"))
        .isEqualTo("http://localhost:4317/v1/metrics");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_METRICS, "http://localhost:4317/foo"))
        .isEqualTo("http://localhost:4317/foo/v1/metrics");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_METRICS, "http://localhost:4317/foo/"))
        .isEqualTo("http://localhost:4317/foo/v1/metrics");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_METRICS, "http://localhost:4317/v1/metrics"))
        .isEqualTo("http://localhost:4317/v1/metrics");
    assertThat(configureHttpProtobufEndpoint(DATA_TYPE_METRICS, "http://localhost:4317/v1/traces"))
        .isEqualTo("http://localhost:4317/v1/traces/v1/metrics");
  }

  private static String configureHttpProtobufEndpoint(String dataType, String configuredEndpoint) {
    AtomicReference<String> endpoint = new AtomicReference<>("");

    OtlpConfigUtil.configureOtlpExporterBuilder(
        dataType,
        DefaultConfigProperties.createForTest(
            ImmutableMap.of(
                "otel.exporter.otlp.protocol", PROTOCOL_HTTP_PROTOBUF,
                "otel.exporter.otlp.endpoint", configuredEndpoint)),
        endpoint::set,
        (value1, value2) -> {},
        value -> {},
        value -> {},
        value -> {});

    return endpoint.get();
  }
}
