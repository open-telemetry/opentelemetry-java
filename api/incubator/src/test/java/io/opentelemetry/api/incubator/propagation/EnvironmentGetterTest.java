/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class EnvironmentGetterTest {

  @RegisterExtension
  LogCapturer logCapturer = LogCapturer.create().captureForType(EnvironmentGetter.class);

  @Test
  void get() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("TRACEPARENT", "val1");
    carrier.put("TRACESTATE", "val2");
    carrier.put("BAGGAGE", "val3");
    carrier.put("OTHER", "val4");

    assertThat(EnvironmentGetter.getInstance().get(carrier, "traceparent")).isEqualTo("val1");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "TRACESTATE")).isEqualTo("val2");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "Baggage")).isEqualTo("val3");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "other")).isEqualTo("val4");
  }

  @Test
  void get_normalization() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("OTEL_TRACE_ID", "val1");
    carrier.put("otel-baggage-key", "val2");

    assertThat(EnvironmentGetter.getInstance().get(carrier, "otel.trace.id")).isEqualTo("val1");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "otel-baggage-key")).isEqualTo("val2");
  }

  @Test
  void get_null() {
    assertThat(EnvironmentGetter.getInstance().get(null, "key")).isNull();
    assertThat(EnvironmentGetter.getInstance().get(Collections.emptyMap(), null)).isNull();
  }

  @Test
  @SuppressLogger(EnvironmentGetter.class)
  void keys_valuesAreNormalized() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("otel.trace.id", "V1");
    carrier.put("otel-baggage-key", "V2");
    carrier.put("OTEL_FOO", "V2");

    // For a carrier containing keys that are both normalized and not normalized, verify all results
    // from keys() return values for get.
    assertThat(EnvironmentGetter.getInstance().keys(carrier))
        .containsExactlyInAnyOrder("OTEL_TRACE_ID", "OTEL_BAGGAGE_KEY", "OTEL_FOO");
    for (String key : EnvironmentGetter.getInstance().keys(carrier)) {
      assertThat(EnvironmentGetter.getInstance().get(carrier, key)).isNotNull();
    }
    assertThat(EnvironmentGetter.getInstance().keys(null)).isEmpty();

    assertThat(logCapturer.size()).isEqualTo(1);
    logCapturer.assertContains("keys() called on EnvironmentGetter");
  }

  @Test
  void get_valuesAreUnmodified() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("KEY1", "simple-value");
    carrier.put("KEY2", "value with spaces");
    carrier.put("KEY3", "value\twith\ttabs");
    carrier.put("KEY4", "value\u0000with\u0001control");
    carrier.put("KEY5", "value\nwith\nnewlines");
    carrier.put("KEY6", "value\u0080non-ascii");

    assertThat(EnvironmentGetter.getInstance().get(carrier, "key1")).isEqualTo("simple-value");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "key2")).isEqualTo("value with spaces");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "key3")).isEqualTo("value\twith\ttabs");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "key4"))
        .isEqualTo("value\u0000with\u0001control");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "key5"))
        .isEqualTo("value\nwith\nnewlines");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "key6"))
        .isEqualTo("value\u0080non-ascii");
  }

  @Test
  void testToString() {
    assertThat(EnvironmentGetter.getInstance().toString()).isEqualTo("EnvironmentGetter");
  }
}
