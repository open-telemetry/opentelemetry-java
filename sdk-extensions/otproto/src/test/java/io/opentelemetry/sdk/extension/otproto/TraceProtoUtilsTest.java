/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.otproto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import org.junit.jupiter.api.Test;

class TraceProtoUtilsTest {

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 0, 0, 0, 'b'};

  @Test
  void toProtoTraceId() {
    ByteString expected = ByteString.copyFrom(TRACE_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoTraceId(TraceId.bytesToHex(TRACE_ID_BYTES)))
        .isEqualTo(expected);
  }

  @Test
  void toProtoSpanId() {
    ByteString expected = ByteString.copyFrom(SPAN_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoSpanId(SpanId.bytesToHex(SPAN_ID_BYTES))).isEqualTo(expected);
  }
}
