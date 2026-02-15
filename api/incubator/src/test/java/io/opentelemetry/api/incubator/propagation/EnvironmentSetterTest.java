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
  void testToString() {
    assertThat(EnvironmentSetter.getInstance().toString()).isEqualTo("EnvironmentSetter");
  }
}
