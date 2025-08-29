/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OtelTraceStateTest {

  @ParameterizedTest
  @CsvSource({
    "a, a",
    "#, #",
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
    "rv:100000000000000, ''",
    "rv:10000000000000, rv:10000000000000",
    "rv:1000000000000, ''",
  })
  void roundTrip(String input, String output) {
    String result = OtelTraceState.parse(TraceState.builder().put("ot", input).build()).serialize();
    assertThat(result).isEqualTo(output);
  }
}
