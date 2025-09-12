/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_RANDOM_VALUE;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.MAX_THRESHOLD;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.TraceState;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OtelTraceStateTest {

  private static String getXString(int len) {
    return Stream.generate(() -> "X").limit(len).collect(Collectors.joining());
  }

  @ParameterizedTest
  @CsvSource({
    "'', ''",
    "a, a",
    "#, #",
    ";, ''",
    "a;, a",
    "a;b;, a;b",
    "animal:bear;food:pizza, animal:bear;food:pizza",
    "rv:1234567890abcd, rv:1234567890abcd",
    "rv:01020304050607, rv:01020304050607",
    "rv:1234567890abcde, ''",
    "th:1234567890abcd, th:1234567890abcd",
    "th:1234567890abcd, th:1234567890abcd",
    "th:10000000000000, th:1",
    "th:1234500000000, th:12345",
    "th:0, th:0",
    "th:100000000000000, ''",
    "th:1234567890abcde, ''",
    "th:, ''",
    "th:x, ''",
    "th:100000000000000, ''",
    "th:10000000000000, th:1",
    "th:1000000000000, th:1",
    "th:100000000000, th:1",
    "th:10000000000, th:1",
    "th:1000000000, th:1",
    "th:100000000, th:1",
    "th:10000000, th:1",
    "th:1000000, th:1",
    "th:100000, th:1",
    "th:10000, th:1",
    "th:1000, th:1",
    "th:100, th:1",
    "th:10, th:1",
    "th:1, th:1",
    "th:10000000000001, th:10000000000001",
    "th:10000000000010, th:1000000000001",
    "rv:x, ''",
    "rv:xxxxxxxxxxxxxx, ''",
    "rv:100000000000000, ''",
    "rv:10000000000000, rv:10000000000000",
    "rv:1000000000000, ''",
  })
  void roundTrip(String input, String output) {
    String result = OtelTraceState.parse(TraceState.builder().put("ot", input).build()).serialize();
    assertThat(result).isEqualTo(output);
  }

  @Test
  void notTooLong() {
    String input = "a:" + getXString(214) + ";rv:1234567890abcd;th:1234567890abcd;x:3";
    String result = OtelTraceState.parse(TraceState.builder().put("ot", input).build()).serialize();
    assertThat(result)
        .isEqualTo("th:1234567890abcd;rv:1234567890abcd;a:" + getXString(214) + ";x:3");
  }

  @Test
  void tooLong() {
    String input = "a:" + getXString(215) + ";rv:1234567890abcd;th:1234567890abcd;x:3";
    String result = OtelTraceState.parse(TraceState.builder().put("ot", input).build()).serialize();
    assertThat(result).isEmpty();
  }

  @Test
  void missing() {
    String result = OtelTraceState.parse(TraceState.getDefault()).serialize();
    assertThat(result).isEmpty();
  }

  @Test
  void emptyMaxThreshold() {
    String result =
        new OtelTraceState(INVALID_RANDOM_VALUE, MAX_THRESHOLD, Collections.emptyList())
            .serialize();
    assertThat(result).isEmpty();
  }
}
