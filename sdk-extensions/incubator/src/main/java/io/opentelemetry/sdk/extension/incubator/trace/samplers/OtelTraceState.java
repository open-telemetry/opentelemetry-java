/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_RANDOM_VALUE;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.MAX_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidRandomValue;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidThreshold;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.internal.TemporaryBuffers;
import io.opentelemetry.api.trace.TraceState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// https://opentelemetry.io/docs/specs/otel/trace/tracestate-handling/
final class OtelTraceState {

  private static final OtelTraceState EMPTY =
      new OtelTraceState(INVALID_RANDOM_VALUE, INVALID_THRESHOLD, Collections.emptyList());

  private static final int MAX_OTEL_TRACE_STATE_LENGTH = 256;
  // visible for testing
  static final int MAX_VALUE_LENGTH = 14; // 56 bits, 4 bits per hex digit

  static final String OTEL_TRACE_STATE_KEY = "ot";

  static OtelTraceState parse(TraceState traceState) {
    String ot = traceState.get(OTEL_TRACE_STATE_KEY);
    if (ot == null || ot.isEmpty() || ot.length() > MAX_OTEL_TRACE_STATE_LENGTH) {
      return EMPTY;
    }

    long threshold = INVALID_THRESHOLD;
    long randomValue = INVALID_RANDOM_VALUE;

    List<String> rest = Collections.emptyList();
    int idx = 0;
    while (idx < ot.length()) {
      int delimIdx = ot.indexOf(';', idx);
      String member = delimIdx != -1 ? ot.substring(idx, delimIdx) : ot.substring(idx);
      if (member.startsWith("th:")) {
        threshold = parseTh(member.substring("th:".length()));
      } else if (member.startsWith("rv:")) {
        randomValue = parseRv(member.substring("rv:".length()));
      } else {
        if (rest.isEmpty()) {
          rest = new ArrayList<>();
        }
        rest.add(member);
      }

      if (delimIdx == -1) {
        break;
      }
      idx = delimIdx + 1;
    }

    return new OtelTraceState(randomValue, threshold, rest);
  }

  private static long parseTh(String th) {
    if (th.isEmpty()
        || th.length() > MAX_VALUE_LENGTH
        || !OtelEncodingUtils.isValidBase16String(th)) {
      return INVALID_THRESHOLD;
    }

    // Fast path for very common all sampling case
    if (th.equals("0")) {
      return 0;
    }

    return OtelEncodingUtils.longFromBase16String(new PaddedValue(th), 0);
  }

  private static long parseRv(String rv) {
    if (rv.length() != MAX_VALUE_LENGTH || !OtelEncodingUtils.isValidBase16String(rv)) {
      return INVALID_RANDOM_VALUE;
    }

    return OtelEncodingUtils.longFromBase16String(new PaddedValue(rv), 0);
  }

  static void serializeTh(long threshold, StringBuilder sb) {
    if (threshold == 0) {
      sb.append('0');
      return;
    }
    // We only need 56 bits, but we can live with one extra byte being encoded.
    char[] buf = TemporaryBuffers.chars(16);
    OtelEncodingUtils.longToBase16String(threshold, buf, 0);
    int startIdx = 2;
    int endIdx = 16;
    for (; endIdx > startIdx; endIdx--) {
      if (buf[endIdx - 1] != '0') {
        break;
      }
    }
    sb.append(buf, startIdx, endIdx - startIdx);
  }

  private static void serializeRv(long randomValue, StringBuilder sb) {
    // We only need 56 bits, but we can live with one extra byte being encoded.
    char[] buf = TemporaryBuffers.chars(16);
    OtelEncodingUtils.longToBase16String(randomValue, buf, 0);
    int startIdx = 2;
    int endIdx = 16;
    sb.append(buf, startIdx, endIdx - startIdx);
  }

  private final long randomValue;
  private final long threshold;
  private final List<String> rest;

  OtelTraceState(long randomValue, long threshold, List<String> rest) {
    this.randomValue = randomValue;
    this.threshold = threshold;
    this.rest = rest;
  }

  long getRandomValue() {
    return randomValue;
  }

  long getThreshold() {
    return threshold;
  }

  List<String> getRest() {
    return rest;
  }

  String serialize() {
    if ((!isValidThreshold(threshold) || threshold == MAX_THRESHOLD)
        && !isValidRandomValue(randomValue)
        && rest.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    if (isValidThreshold(threshold) && threshold != MAX_THRESHOLD) {
      sb.append("th:");
      serializeTh(threshold, sb);
      sb.append(';');
    }
    if (isValidRandomValue(randomValue)) {
      sb.append("rv:");
      serializeRv(randomValue, sb);
      sb.append(';');
    }
    for (String member : rest) {
      sb.append(member);
      sb.append(';');
    }

    // Trim trailing semicolon
    sb.setLength(sb.length() - 1);
    return sb.toString();
  }

  private static class PaddedValue implements CharSequence {
    private final String value;

    PaddedValue(String value) {
      this.value = value;
    }

    @Override
    public int length() {
      return 16;
    }

    @Override
    public char charAt(int index) {
      if (index < 2) {
        return '0';
      }
      index -= 2;
      if (index < value.length()) {
        return value.charAt(index);
      }
      return '0';
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      throw new UnsupportedOperationException();
    }
  }
}
