/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.api.common.AnyValueType;
import io.opentelemetry.api.common.KeyAnyValue;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class AnyValueBodyTest {

  @Test
  @SuppressWarnings("DoubleBraceInitialization")
  void anyValueBody() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider provider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    Logger logger = provider.get(AnyValueBodyTest.class.getName());

    // AnyValue can be a primitive type, like a string, long, double, boolean
    extendedLogRecordBuilder(logger).setBody(AnyValue.of(1)).emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getAnyValueBody())
                  .isNotNull()
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(AnyValueType.LONG);
                        assertThat((Long) body.getValue()).isEqualTo(1L);
                      });
            });
    exporter.reset();

    // ...or a byte array of raw data
    extendedLogRecordBuilder(logger)
        .setBody(AnyValue.of("hello world".getBytes(StandardCharsets.UTF_8)))
        .emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getAnyValueBody())
                  .isNotNull()
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(AnyValueType.BYTES);
                        assertThat((ByteBuffer) body.getValue())
                            .isEqualTo(
                                ByteBuffer.wrap("hello world".getBytes(StandardCharsets.UTF_8)));
                      });
            });
    exporter.reset();

    // But most commonly it will be used to represent complex structured like a map
    extendedLogRecordBuilder(logger)
        .setBody(
            // The protocol data structure uses a repeated KeyValue to represent a map:
            // https://github.com/open-telemetry/opentelemetry-proto/blob/ac3242b03157295e4ee9e616af53b81517b06559/opentelemetry/proto/common/v1/common.proto#L59
            // The comment says that keys aren't allowed to repeat themselves, and because its
            // represented as a repeated KeyValue, we need to at least offer the ability to preserve
            // order.
            // Accepting a Map<String, AnyValue<?>> makes for a cleaner API, but ordering of the
            // entries is lost. To accommodate use cases where ordering should be preserved we
            // accept an array of key value pairs, but also a map based alternative (see the
            // key_value_list_key entry).
            AnyValue.of(
                KeyAnyValue.of("str_key", AnyValue.of("value")),
                KeyAnyValue.of("bool_key", AnyValue.of(true)),
                KeyAnyValue.of("long_key", AnyValue.of(1L)),
                KeyAnyValue.of("double_key", AnyValue.of(1.1)),
                KeyAnyValue.of("bytes_key", AnyValue.of("bytes".getBytes(StandardCharsets.UTF_8))),
                KeyAnyValue.of(
                    "arr_key",
                    AnyValue.of(AnyValue.of("entry1"), AnyValue.of(2), AnyValue.of(3.3))),
                KeyAnyValue.of(
                    "key_value_list_key",
                    AnyValue.of(
                        new LinkedHashMap<String, AnyValue<?>>() {
                          {
                            put("child_str_key1", AnyValue.of("child_value1"));
                            put("child_str_key2", AnyValue.of("child_value2"));
                          }
                        }))))
        .emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getAnyValueBody())
                  .isNotNull()
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(AnyValueType.KEY_VALUE_LIST);
                        assertThat(body)
                            .isEqualTo(
                                AnyValue.of(
                                    KeyAnyValue.of("str_key", AnyValue.of("value")),
                                    KeyAnyValue.of("bool_key", AnyValue.of(true)),
                                    KeyAnyValue.of("long_key", AnyValue.of(1L)),
                                    KeyAnyValue.of("double_key", AnyValue.of(1.1)),
                                    KeyAnyValue.of(
                                        "bytes_key",
                                        AnyValue.of("bytes".getBytes(StandardCharsets.UTF_8))),
                                    KeyAnyValue.of(
                                        "arr_key",
                                        AnyValue.of(
                                            AnyValue.of("entry1"),
                                            AnyValue.of(2),
                                            AnyValue.of(3.3))),
                                    KeyAnyValue.of(
                                        "key_value_list_key",
                                        AnyValue.of(
                                            new LinkedHashMap<String, AnyValue<?>>() {
                                              {
                                                put("child_str_key1", AnyValue.of("child_value1"));
                                                put("child_str_key2", AnyValue.of("child_value2"));
                                              }
                                            }))));
                        assertThat(body.asString())
                            .isEqualTo(
                                "["
                                    + "str_key=value, "
                                    + "bool_key=true, "
                                    + "long_key=1, "
                                    + "double_key=1.1, "
                                    + "bytes_key=Ynl0ZXM=, "
                                    + "arr_key=[entry1, 2, 3.3], "
                                    + "key_value_list_key=[child_str_key1=child_value1, child_str_key2=child_value2]"
                                    + "]");
                      });
            });
    exporter.reset();

    // ..or an array (optionally with heterogeneous types)
    extendedLogRecordBuilder(logger)
        .setBody(AnyValue.of(AnyValue.of("entry1"), AnyValue.of("entry2"), AnyValue.of(3)))
        .emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getAnyValueBody())
                  .isNotNull()
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(AnyValueType.ARRAY);
                        assertThat(body)
                            .isEqualTo(
                                AnyValue.of(
                                    AnyValue.of("entry1"), AnyValue.of("entry2"), AnyValue.of(3)));
                      });
            });
    exporter.reset();
  }

  ExtendedLogRecordBuilder extendedLogRecordBuilder(Logger logger) {
    return (ExtendedLogRecordBuilder) logger.logRecordBuilder();
  }
}
