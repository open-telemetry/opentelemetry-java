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

    assertThat(EnvironmentGetter.INSTANCE.get(carrier, "traceparent")).isEqualTo("val1");
    assertThat(EnvironmentGetter.INSTANCE.get(carrier, "TRACESTATE")).isEqualTo("val2");
    assertThat(EnvironmentGetter.INSTANCE.get(carrier, "Baggage")).isEqualTo("val3");
    assertThat(EnvironmentGetter.INSTANCE.get(carrier, "other")).isEqualTo("val4");
  }

  @Test
  void get_null() {
    assertThat(EnvironmentGetter.INSTANCE.get(null, "key")).isNull();
    assertThat(EnvironmentGetter.INSTANCE.get(Collections.emptyMap(), null)).isNull();
  }

  @Test
  void keys() {
    Map<String, String> carrier = new HashMap<>();
    carrier.put("K1", "V1");
    carrier.put("K2", "V2");

    assertThat(EnvironmentGetter.INSTANCE.keys(carrier)).containsExactlyInAnyOrder("K1", "K2");
    assertThat(EnvironmentGetter.INSTANCE.keys(null)).isEmpty();
  }

  @Test
  void testToString() {
    assertThat(EnvironmentGetter.INSTANCE.toString()).isEqualTo("EnvironmentGetter");
  }
}
