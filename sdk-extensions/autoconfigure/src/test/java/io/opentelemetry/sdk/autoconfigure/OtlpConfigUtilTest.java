/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.DATA_TYPE_TRACES;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class OtlpConfigUtilTest {

  @Test
  void getOtlpProtocolDefault() {
    assertThat(
            OtlpConfigUtil.getOtlpProtocol(
                DATA_TYPE_TRACES, DefaultConfigProperties.createForTest(Collections.emptyMap())))
        .isEqualTo("grpc");

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
}
