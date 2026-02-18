/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvironmentSetterTest {

  @Test
  void set() {
    Map<String, String> carrier = new HashMap<>();
    EnvironmentSetter.getInstance().set(carrier, "traceparent", "val1");
    EnvironmentSetter.getInstance().set(carrier, "TRACESTATE", "val2");
    EnvironmentSetter.getInstance().set(carrier, "Baggage", "val3");

    assertThat(carrier).containsEntry("TRACEPARENT", "val1");
    assertThat(carrier).containsEntry("TRACESTATE", "val2");
    assertThat(carrier).containsEntry("BAGGAGE", "val3");
  }

  @Test
  void set_sanitization() {
    Map<String, String> carrier = new HashMap<>();
    EnvironmentSetter.getInstance().set(carrier, "otel.trace.id", "val1");
    EnvironmentSetter.getInstance().set(carrier, "otel-baggage-key", "val2");

    assertThat(carrier).containsEntry("OTEL_TRACE_ID", "val1");
    assertThat(carrier).containsEntry("OTEL_BAGGAGE_KEY", "val2");
  }

  @Test
  void set_null() {
    Map<String, String> carrier = new HashMap<>();
    EnvironmentSetter.getInstance().set(null, "key", "val");
    EnvironmentSetter.getInstance().set(carrier, null, "val");
    EnvironmentSetter.getInstance().set(carrier, "key", null);
    assertThat(carrier).isEmpty();
  }

  @Test
  void set_validHeaderValues() {
    Map<String, String> carrier = new HashMap<>();
    // Printable ASCII and tab are valid per RFC 9110
    EnvironmentSetter.getInstance().set(carrier, "key1", "simple-value");
    EnvironmentSetter.getInstance().set(carrier, "key2", "value with spaces");
    EnvironmentSetter.getInstance().set(carrier, "key3", "value\twith\ttabs");

    assertThat(carrier).containsEntry("KEY1", "simple-value");
    assertThat(carrier).containsEntry("KEY2", "value with spaces");
    assertThat(carrier).containsEntry("KEY3", "value\twith\ttabs");
  }

  @Test
  void set_invalidHeaderValues() {
    Map<String, String> carrier = new HashMap<>();
    // Control characters and non-ASCII are invalid per RFC 9110
    EnvironmentSetter.getInstance().set(carrier, "key1", "value\u0000with\u0001control");
    EnvironmentSetter.getInstance().set(carrier, "key2", "value\nwith\nnewlines");
    EnvironmentSetter.getInstance().set(carrier, "key3", "value\rwith\rcarriage");
    EnvironmentSetter.getInstance().set(carrier, "key4", "value\u0080non-ascii");

    assertThat(carrier).isEmpty();
  }

  @Test
  void testToString() {
    assertThat(EnvironmentSetter.getInstance().toString()).isEqualTo("EnvironmentSetter");
  }
}
