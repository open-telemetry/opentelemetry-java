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
import org.junit.jupiter.api.Test;

/** Tests for {@link OpenTelemetryBinaryFormatImpl}. */
public class OpenTelemetryBinaryFormatImplTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79};
  private static final TraceId TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {97, 98, 99, 100, 101, 102, 103, 104};
  private static final SpanId SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final byte TRACE_OPTIONS_BYTES = 1;
  private static final TraceOptions TRACE_OPTIONS = TraceOptions.fromByte(TRACE_OPTIONS_BYTES);
  private static final Tracestate TRACESTATE = Tracestate.builder().build();
  private static final byte[] EXAMPLE_BYTES =
      new byte[] {
        0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100,
        101, 102, 103, 104, 2, 1
      };
  private static final SpanContext EXAMPLE_SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS, TRACESTATE);
  private final BinaryFormat binaryFormat = new OpenTelemetryBinaryFormatImpl();

  private void testSpanContextConversion(SpanContext spanContext) throws SpanContextParseException {
    SpanContext propagatedBinarySpanContext =
        binaryFormat.fromByteArray(binaryFormat.toByteArray(spanContext));

    assertThat(propagatedBinarySpanContext).isEqualTo(spanContext);
  }

  @Test
  public void propagate_SpanContextTracingEnabled() throws SpanContextParseException {
    testSpanContextConversion(
        SpanContext.create(
            TRACE_ID, SPAN_ID, TraceOptions.builder().setIsSampled(true).build(), TRACESTATE));
  }

  @Test
  public void propagate_SpanContextNoTracing() throws SpanContextParseException {
    testSpanContextConversion(
        SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE));
  }

  @Test
  public void toBinaryValue_NullSpanContext() {
    assertThrows(NullPointerException.class, () -> binaryFormat.toByteArray(null), "spanContext");
  }

  @Test
  public void toBinaryValue_InvalidSpanContext() {
    assertThat(binaryFormat.toByteArray(SpanContext.INVALID))
        .isEqualTo(
            new byte[] {
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0
            });
  }

  @Test
  public void fromBinaryValue_BinaryExampleValue() throws SpanContextParseException {
    assertThat(binaryFormat.fromByteArray(EXAMPLE_BYTES)).isEqualTo(EXAMPLE_SPAN_CONTEXT);
  }

  @Test
  public void fromBinaryValue_NullInput() {
    assertThrows(NullPointerException.class, () -> binaryFormat.toByteArray(null), "spanContext");
  }

  @Test
  public void fromBinaryValue_EmptyInput() {
    assertThrows(
        SpanContextParseException.class,
        () -> binaryFormat.fromByteArray(new byte[0]),
        "Unsupported version.");
  }

  @Test
  public void fromBinaryValue_UnsupportedVersionId() {
    assertThrows(
        SpanContextParseException.class,
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
        SpanContextParseException.class,
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
        SpanContextParseException.class,
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
        SpanContextParseException.class,
        () ->
            binaryFormat.fromByteArray(
                new byte[] {0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76}),
        "Invalid input: truncated");
  }

  @Test
  public void fromBinaryValue_ShorterSpanId() {
    assertThrows(
        SpanContextParseException.class,
        () -> binaryFormat.fromByteArray(new byte[] {0, 1, 97, 98, 99, 100, 101, 102, 103}),
        "Invalid input: truncated");
  }

  @Test
  public void fromBinaryValue_ShorterTraceOptions() {
    assertThrows(
        SpanContextParseException.class,
        () ->
            binaryFormat.fromByteArray(
                new byte[] {
                  0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98,
                  99, 100, 101, 102, 103, 104, 2
                }),
        "Invalid input: truncated");
  }

  @Test
  public void fromBinaryValue_MissingTraceOptionsOk() throws SpanContextParseException {
    SpanContext extracted =
        binaryFormat.fromByteArray(
            new byte[] {
              0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99,
              100, 101, 102, 103, 104
            });

    assertThat(extracted.isValid()).isTrue();
    assertThat(extracted.getTraceOptions()).isEqualTo(TraceOptions.DEFAULT);
  }
}
