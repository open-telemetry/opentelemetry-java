/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.trace.propagation;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link io.opentelemetry.trace.propagation.BinaryTraceContext}. */
@RunWith(JUnit4.class)
public class BinaryTraceContextTest {

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79};
  private static final TraceId TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES, 0);
  private static final byte[] SPAN_ID_BYTES = new byte[] {97, 98, 99, 100, 101, 102, 103, 104};
  private static final SpanId SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES, 0);
  private static final byte TRACE_OPTIONS_BYTES = 1;
  private static final TraceFlags TRACE_OPTIONS = TraceFlags.fromByte(TRACE_OPTIONS_BYTES);
  private static final byte[] EXAMPLE_BYTES =
      new byte[] {
        0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100,
        101, 102, 103, 104, 2, 1
      };
  private static final SpanContext INVALID_SPAN_CONTEXT = DefaultSpan.getInvalid().getContext();
  @Rule public ExpectedException expectedException = ExpectedException.none();
  private final BinaryFormat<SpanContext> binaryFormat = new BinaryTraceContext();

  private void testSpanContextConversion(SpanContext spanContext) {
    SpanContext propagatedBinarySpanContext =
        binaryFormat.fromByteArray(binaryFormat.toByteArray(spanContext));

    assertThat(propagatedBinarySpanContext.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(propagatedBinarySpanContext.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(propagatedBinarySpanContext.getTraceFlags()).isEqualTo(spanContext.getTraceFlags());
  }

  @Test
  public void propagate_SpanContextTracingEnabled() {
    testSpanContextConversion(
        SpanContext.create(
            TRACE_ID,
            SPAN_ID,
            TraceFlags.builder().setIsSampled(true).build(),
            TraceState.getDefault()));
  }

  @Test
  public void propagate_SpanContextNoTracing() {
    testSpanContextConversion(
        SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()));
  }

  @Test(expected = NullPointerException.class)
  public void toBinaryValue_NullSpanContext() {
    binaryFormat.toByteArray(null);
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
  public void fromBinaryValue_BinaryExampleValue() {
    assertThat(binaryFormat.fromByteArray(EXAMPLE_BYTES))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TRACE_OPTIONS, TraceState.getDefault()));
  }

  @Test(expected = NullPointerException.class)
  public void fromBinaryValue_NullInput() {
    binaryFormat.fromByteArray(null);
  }

  @Test
  public void fromBinaryValue_EmptyInput() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Unsupported version.");
    binaryFormat.fromByteArray(new byte[0]);
  }

  @Test
  public void fromBinaryValue_UnsupportedVersionId() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Unsupported version.");
    binaryFormat.fromByteArray(
        new byte[] {
          66, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 97, 98, 99, 100, 101,
          102, 103, 104, 1
        });
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdFirst() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(
        "Invalid input: expected trace ID at offset "
            + BinaryTraceContext.TRACE_ID_FIELD_ID_OFFSET);
    binaryFormat.fromByteArray(
        new byte[] {
          0, 4, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100,
          101, 102, 103, 104, 2, 1
        });
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdSecond() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(
        "Invalid input: expected span ID at offset " + BinaryTraceContext.SPAN_ID_FIELD_ID_OFFSET);
    binaryFormat.fromByteArray(
        new byte[] {
          0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 3, 97, 98, 99, 100,
          101, 102, 103, 104, 2, 1
        });
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdThird_skipped() {
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
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Invalid input: truncated");
    binaryFormat.fromByteArray(
        new byte[] {0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76});
  }

  @Test
  public void fromBinaryValue_ShorterSpanId() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Invalid input: truncated");
    binaryFormat.fromByteArray(new byte[] {0, 1, 97, 98, 99, 100, 101, 102, 103});
  }

  @Test
  public void fromBinaryValue_ShorterTraceFlags() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Invalid input: truncated");
    binaryFormat.fromByteArray(
        new byte[] {
          0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100,
          101, 102, 103, 104, 2
        });
  }

  @Test
  public void fromBinaryValue_MissingTraceFlagsOk() {
    SpanContext extracted =
        binaryFormat.fromByteArray(
            new byte[] {
              0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99,
              100, 101, 102, 103, 104
            });

    assertThat(extracted.isValid()).isTrue();
    assertThat(extracted.getTraceFlags()).isEqualTo(TraceFlags.getDefault());
  }
}
