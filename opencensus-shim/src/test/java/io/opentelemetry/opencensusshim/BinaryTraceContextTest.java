/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.SpanContextParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link OpenTelemetryBinaryFormatImpl}. */
@RunWith(JUnit4.class)
public class BinaryTraceContextTest {

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79};
  private static final TraceId TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES, 0);
  private static final byte[] SPAN_ID_BYTES = new byte[] {97, 98, 99, 100, 101, 102, 103, 104};
  private static final SpanId SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES, 0);
  private static final byte[] EXAMPLE_BYTES =
      new byte[] {
        0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100,
        101, 102, 103, 104, 2, 1
      };
  private static final io.opencensus.trace.SpanContext INVALID_SPAN_CONTEXT =
      io.opencensus.trace.SpanContext.INVALID;
  private final BinaryFormat binaryFormat = new OpenTelemetryBinaryFormatImpl();

  private void testSpanContextConversion(io.opencensus.trace.SpanContext spanContext)
      throws SpanContextParseException {
    io.opencensus.trace.SpanContext propagatedBinarySpanContext =
        binaryFormat.fromByteArray(binaryFormat.toByteArray(spanContext));

    assertThat(propagatedBinarySpanContext.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(propagatedBinarySpanContext.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(propagatedBinarySpanContext.getTracestate()).isEqualTo(spanContext.getTracestate());
    assertThat(propagatedBinarySpanContext.getTraceOptions())
        .isEqualTo(spanContext.getTraceOptions());
  }

  @Test
  public void propagate_SpanContextTracingEnabled() throws SpanContextParseException {
    testSpanContextConversion(
        io.opencensus.trace.SpanContext.create(
            TRACE_ID,
            SPAN_ID,
            TraceOptions.builder().setIsSampled(true).build(),
            Tracestate.builder().build()));
  }

  @Test
  public void propagate_SpanContextNoTracing() throws SpanContextParseException {
    testSpanContextConversion(
        io.opencensus.trace.SpanContext.create(
            TRACE_ID,
            SPAN_ID,
            TraceOptions.builder().setIsSampled(true).build(),
            Tracestate.builder().build()));
  }

  @Test
  public void toBinaryValue_NullSpanContext() {
    assertThrows(NullPointerException.class, () -> binaryFormat.toByteArray(null), "spanContext");
  }

  @Test
  public void toBinaryValue_InvalidSpanContext() {
    assertThat(binaryFormat.toByteArray(INVALID_SPAN_CONTEXT))
        .isEqualTo(
            new byte[] {
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0
            });
  }

  @Test
  public void fromBinaryValue_BinaryExampleValue() throws SpanContextParseException {
    assertThat(binaryFormat.fromByteArray(EXAMPLE_BYTES))
        .isEqualTo(
            SpanContext.create(
                TRACE_ID,
                SPAN_ID,
                TraceOptions.builder().setIsSampled(true).build(),
                Tracestate.builder().build()));
  }

  @Test
  public void fromBinaryValue_NullInput() {
    assertThrows(NullPointerException.class, () -> binaryFormat.fromByteArray(null), "bytes");
  }

  @Test
  public void fromBinaryValue_EmptyInput() {
    assertThrows(
        IllegalArgumentException.class,
        () -> binaryFormat.fromByteArray(new byte[0]),
        "Unsupported version.");
  }

  @Test
  public void fromBinaryValue_UnsupportedVersionId() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            binaryFormat.fromByteArray(
                new byte[] {
                  66, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 97, 98, 99,
                  100, 101, 102, 103, 104, 1
                }),
        "Unsupported version.");
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdFirst() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            binaryFormat.fromByteArray(
                new byte[] {
                  0, 4, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98,
                  99, 100, 101, 102, 103, 104, 2, 1
                }),
        "Invalid input: expected trace ID at offset "
            + OpenTelemetryBinaryFormatImpl.TRACE_ID_FIELD_ID_OFFSET);
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdSecond() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            binaryFormat.fromByteArray(
                new byte[] {
                  0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 3, 97, 98,
                  99, 100, 101, 102, 103, 104, 2, 1
                }),
        "Invalid input: expected span ID at offset "
            + OpenTelemetryBinaryFormatImpl.SPAN_ID_FIELD_ID_OFFSET);
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdThird_skipped() throws SpanContextParseException {
    assertThat(
            binaryFormat
                .fromByteArray(
                    new byte[] {
                      0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97,
                      98, 99, 100, 101, 102, 103, 104, 0, 1
                    })
                .isValid())
        .isTrue();
  }

  @Test
  public void fromBinaryValue_ShorterTraceId() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            binaryFormat.fromByteArray(
                new byte[] {0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76}),
        "Invalid input: truncated");
  }

  @Test
  public void fromBinaryValue_ShorterSpanId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> binaryFormat.fromByteArray(new byte[] {0, 1, 97, 98, 99, 100, 101, 102, 103}),
        "Invalid input: truncated");
  }

  @Test
  public void fromBinaryValue_ShorterTraceFlags() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            binaryFormat.fromByteArray(
                new byte[] {
                  0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98,
                  99, 100, 101, 102, 103, 104, 2
                }),
        "Invalid input: truncated");
  }

  @Test
  public void fromBinaryValue_MissingTraceFlagsOk() throws SpanContextParseException {
    io.opencensus.trace.SpanContext extracted =
        binaryFormat.fromByteArray(
            new byte[] {
              0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99,
              100, 101, 102, 103, 104
            });

    assertThat(extracted.isValid()).isTrue();
    assertThat(extracted.getTraceOptions()).isEqualTo(TraceOptions.builder().build());
  }
}
