/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace.propagation.internal;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.TraceStateBuilder;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the {@code tracestate} header encoding and decoding as defined by the <a
 * href="https://www.w3.org/TR/trace-context-1/#tracestate-header">W3C Trace Context</a>
 * recommendation.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public final class W3CTraceContextEncoding {

  private W3CTraceContextEncoding() {}

  private static final int TRACESTATE_MAX_SIZE = 512;
  private static final int TRACESTATE_MAX_MEMBERS = 32;
  private static final char TRACESTATE_KEY_VALUE_DELIMITER = '=';
  private static final char TRACESTATE_ENTRY_DELIMITER = ',';
  private static final Pattern TRACESTATE_ENTRY_DELIMITER_SPLIT_PATTERN =
      Pattern.compile("[ \t]*" + TRACESTATE_ENTRY_DELIMITER + "[ \t]*");

  /**
   * Decodes a trace state header into a {@link TraceState} object.
   *
   * @throws IllegalArgumentException if {@code traceStateHeader} does not comply with the
   *     specification
   */
  public static TraceState decodeTraceState(String traceStateHeader) {
    TraceStateBuilder traceStateBuilder = TraceState.builder();
    String[] listMembers = TRACESTATE_ENTRY_DELIMITER_SPLIT_PATTERN.split(traceStateHeader);
    checkArgument(
        listMembers.length <= TRACESTATE_MAX_MEMBERS, "TraceState has too many elements.");
    // Iterate in reverse order because when call builder set the elements is added in the
    // front of the list.
    for (int i = listMembers.length - 1; i >= 0; i--) {
      String listMember = listMembers[i];
      int index = listMember.indexOf(TRACESTATE_KEY_VALUE_DELIMITER);
      checkArgument(index != -1, "Invalid TraceState list-member format.");
      traceStateBuilder.put(listMember.substring(0, index), listMember.substring(index + 1));
    }
    TraceState traceState = traceStateBuilder.build();
    if (traceState.size() != listMembers.length) {
      // Validation failure, drop the tracestate
      return TraceState.getDefault();
    }
    return traceState;
  }

  /** Return the trace state encoded as a string according to the W3C specification. */
  public static String encodeTraceState(TraceState traceState) {
    if (traceState.isEmpty()) {
      return "";
    }
    StringBuilder builder = new StringBuilder(TRACESTATE_MAX_SIZE);
    traceState.forEach(
        (key, value) -> {
          if (builder.length() != 0) {
            builder.append(TRACESTATE_ENTRY_DELIMITER);
          }
          builder.append(key).append(TRACESTATE_KEY_VALUE_DELIMITER).append(value);
        });
    return builder.toString();
  }
}
