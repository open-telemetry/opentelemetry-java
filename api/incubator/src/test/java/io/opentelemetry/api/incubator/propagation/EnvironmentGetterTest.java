/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvironmentGetterTest {

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
  void get_sanitization() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("OTEL_TRACE_ID", "val1");
    carrier.put("OTEL_BAGGAGE_KEY", "val2");

    assertThat(EnvironmentGetter.getInstance().get(carrier, "otel.trace.id")).isEqualTo("val1");
    assertThat(EnvironmentGetter.getInstance().get(carrier, "otel-baggage-key")).isEqualTo("val2");
  }

  @Test
  void get_null() {
    assertThat(EnvironmentGetter.getInstance().get(null, "key")).isNull();
    assertThat(EnvironmentGetter.getInstance().get(Collections.emptyMap(), null)).isNull();
  }

  @Test
  void keys() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("K1", "V1");
    carrier.put("K2", "V2");

    assertThat(EnvironmentGetter.getInstance().keys(carrier)).containsExactlyInAnyOrder("K1", "K2");
    assertThat(EnvironmentGetter.getInstance().keys(null)).isEmpty();
  }

  @Test
  void testToString() {
    assertThat(EnvironmentGetter.getInstance().toString()).isEqualTo("EnvironmentGetter");
  }
}
