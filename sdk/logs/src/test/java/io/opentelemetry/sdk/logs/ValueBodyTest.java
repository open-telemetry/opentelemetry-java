/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class ValueBodyTest {

  @Test
  @SuppressWarnings("DoubleBraceInitialization")
  void valueBody() {
    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider provider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build();
    Logger logger = provider.get(ValueBodyTest.class.getName());

    // Value can be a primitive type, like a string, long, double, boolean
    logger.logRecordBuilder().setBody(Value.of(1)).emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getBodyValue())
                  .isNotNull()
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(ValueType.LONG);
                        assertThat((Long) body.getValue()).isEqualTo(1L);
                      });
            });
    exporter.reset();

    // ...or a byte array of raw data
    logger
        .logRecordBuilder()
        .setBody(Value.of("hello world".getBytes(StandardCharsets.UTF_8)))
        .emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getBodyValue())
                  .isNotNull()
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(ValueType.BYTES);
                        assertThat((ByteBuffer) body.getValue())
                            .isEqualTo(
                                ByteBuffer.wrap("hello world".getBytes(StandardCharsets.UTF_8)));
                      });
            });
    exporter.reset();

    // But most commonly it will be used to represent complex structured like a map
    logger
        .logRecordBuilder()
        .setBody(
            // The protocol data structure uses a repeated KeyValue to represent a map:
            // https://github.com/open-telemetry/opentelemetry-proto/blob/ac3242b03157295e4ee9e616af53b81517b06559/opentelemetry/proto/common/v1/common.proto#L59
            // The comment says that keys aren't allowed to repeat themselves, and because its
            // represented as a repeated KeyValue, we need to at least offer the ability to preserve
            // order.
            // Accepting a Map<String, Value<?>> makes for a cleaner API, but ordering of the
            // entries is lost. To accommodate use cases where ordering should be preserved we
            // accept an array of key value pairs, but also a map based alternative (see the
            // key_value_list_key entry).
            Value.of(
                KeyValue.of("str_key", Value.of("value")),
                KeyValue.of("bool_key", Value.of(true)),
                KeyValue.of("long_key", Value.of(1L)),
                KeyValue.of("double_key", Value.of(1.1)),
                KeyValue.of("bytes_key", Value.of("bytes".getBytes(StandardCharsets.UTF_8))),
                KeyValue.of("arr_key", Value.of(Value.of("entry1"), Value.of(2), Value.of(3.3))),
                KeyValue.of(
                    "key_value_list_key",
                    Value.of(
                        new LinkedHashMap<String, Value<?>>() {
                          {
                            put("child_str_key1", Value.of("child_value1"));
                            put("child_str_key2", Value.of("child_value2"));
                          }
                        }))))
        .emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getBodyValue())
                  .isNotNull()
                  // TODO: use fluent asserts when available. See
                  // https://github.com/open-telemetry/opentelemetry-java/pull/6509
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(ValueType.KEY_VALUE_LIST);
                        assertThat(body)
                            .isEqualTo(
                                Value.of(
                                    KeyValue.of("str_key", Value.of("value")),
                                    KeyValue.of("bool_key", Value.of(true)),
                                    KeyValue.of("long_key", Value.of(1L)),
                                    KeyValue.of("double_key", Value.of(1.1)),
                                    KeyValue.of(
                                        "bytes_key",
                                        Value.of("bytes".getBytes(StandardCharsets.UTF_8))),
                                    KeyValue.of(
                                        "arr_key",
                                        Value.of(Value.of("entry1"), Value.of(2), Value.of(3.3))),
                                    KeyValue.of(
                                        "key_value_list_key",
                                        Value.of(
                                            new LinkedHashMap<String, Value<?>>() {
                                              {
                                                put("child_str_key1", Value.of("child_value1"));
                                                put("child_str_key2", Value.of("child_value2"));
                                              }
                                            }))));
                        assertThat(body.asString())
                            .isEqualTo(
                                "{"
                                    + "\"str_key\":\"value\","
                                    + "\"bool_key\":true,"
                                    + "\"long_key\":1,"
                                    + "\"double_key\":1.1,"
                                    + "\"bytes_key\":\"Ynl0ZXM=\","
                                    + "\"arr_key\":[\"entry1\",2,3.3],"
                                    + "\"key_value_list_key\":{\"child_str_key1\":\"child_value1\",\"child_str_key2\":\"child_value2\"}"
                                    + "}");
                      });
            });
    exporter.reset();

    // ..or an array (optionally with heterogeneous types)
    logger
        .logRecordBuilder()
        .setBody(Value.of(Value.of("entry1"), Value.of("entry2"), Value.of(3)))
        .emit();
    assertThat(exporter.getFinishedLogRecordItems())
        .hasSize(1)
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getBodyValue())
                  .isNotNull()
                  .satisfies(
                      body -> {
                        assertThat(body.getType()).isEqualTo(ValueType.ARRAY);
                        assertThat(body)
                            .isEqualTo(
                                Value.of(Value.of("entry1"), Value.of("entry2"), Value.of(3)));
                      });
            });
    exporter.reset();
  }
}
