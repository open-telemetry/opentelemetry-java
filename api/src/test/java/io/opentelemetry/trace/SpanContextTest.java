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

package io.opentelemetry.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpanContext}. */
@RunWith(JUnit4.class)
public class SpanContextTest {
  private static final byte[] firstTraceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondTraceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0, 0, 0, 0, 0};
  private static final byte[] firstSpanIdBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondSpanIdBytes = new byte[] {'0', 0, 0, 0, 0, 0, 0, 0};
  private static final Tracestate firstTracestate = Tracestate.builder().set("foo", "bar").build();
  private static final Tracestate secondTracestate = Tracestate.builder().set("foo", "baz").build();
  private static final Tracestate emptyTracestate = Tracestate.builder().build();
  private static final SpanContext first =
      SpanContext.create(
          TraceId.fromBytes(firstTraceIdBytes, 0),
          SpanId.fromBytes(firstSpanIdBytes, 0),
          TraceFlags.getDefault(),
          firstTracestate);
  private static final SpanContext second =
      SpanContext.create(
          TraceId.fromBytes(secondTraceIdBytes, 0),
          SpanId.fromBytes(secondSpanIdBytes, 0),
          TraceFlags.builder().setIsSampled(true).build(),
          secondTracestate);

  @Test
  public void invalidSpanContext() {
    assertThat(SpanContext.getInvalid().getTraceId()).isEqualTo(TraceId.getInvalid());
    assertThat(SpanContext.getInvalid().getSpanId()).isEqualTo(SpanId.getInvalid());
    assertThat(SpanContext.getInvalid().getTraceFlags()).isEqualTo(TraceFlags.getDefault());
  }

  @Test
  public void isValid() {
    assertThat(SpanContext.getInvalid().isValid()).isFalse();
    assertThat(
            SpanContext.create(
                    TraceId.fromBytes(firstTraceIdBytes, 0),
                    SpanId.getInvalid(),
                    TraceFlags.getDefault(),
                    emptyTracestate)
                .isValid())
        .isFalse();
    assertThat(
            SpanContext.create(
                    TraceId.getInvalid(),
                    SpanId.fromBytes(firstSpanIdBytes, 0),
                    TraceFlags.getDefault(),
                    emptyTracestate)
                .isValid())
        .isFalse();
    assertThat(first.isValid()).isTrue();
    assertThat(second.isValid()).isTrue();
  }

  @Test
  public void getTraceId() {
    assertThat(first.getTraceId()).isEqualTo(TraceId.fromBytes(firstTraceIdBytes, 0));
    assertThat(second.getTraceId()).isEqualTo(TraceId.fromBytes(secondTraceIdBytes, 0));
  }

  @Test
  public void getSpanId() {
    assertThat(first.getSpanId()).isEqualTo(SpanId.fromBytes(firstSpanIdBytes, 0));
    assertThat(second.getSpanId()).isEqualTo(SpanId.fromBytes(secondSpanIdBytes, 0));
  }

  @Test
  public void getTraceFlags() {
    assertThat(first.getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(second.getTraceFlags()).isEqualTo(TraceFlags.builder().setIsSampled(true).build());
  }

  @Test
  public void getTracestate() {
    assertThat(first.getTracestate()).isEqualTo(firstTracestate);
    assertThat(second.getTracestate()).isEqualTo(secondTracestate);
  }

  @Test
  public void spanContext_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        first,
        SpanContext.create(
            TraceId.fromBytes(firstTraceIdBytes, 0),
            SpanId.fromBytes(firstSpanIdBytes, 0),
            TraceFlags.getDefault(),
            emptyTracestate),
        SpanContext.create(
            TraceId.fromBytes(firstTraceIdBytes, 0),
            SpanId.fromBytes(firstSpanIdBytes, 0),
            TraceFlags.builder().setIsSampled(false).build(),
            firstTracestate));
    tester.addEqualityGroup(
        second,
        SpanContext.create(
            TraceId.fromBytes(secondTraceIdBytes, 0),
            SpanId.fromBytes(secondSpanIdBytes, 0),
            TraceFlags.builder().setIsSampled(true).build(),
            secondTracestate));
    tester.testEquals();
  }

  @Test
  public void spanContext_ToString() {
    assertThat(first.toString()).contains(TraceId.fromBytes(firstTraceIdBytes, 0).toString());
    assertThat(first.toString()).contains(SpanId.fromBytes(firstSpanIdBytes, 0).toString());
    assertThat(first.toString()).contains(TraceFlags.getDefault().toString());
    assertThat(second.toString()).contains(TraceId.fromBytes(secondTraceIdBytes, 0).toString());
    assertThat(second.toString()).contains(SpanId.fromBytes(secondSpanIdBytes, 0).toString());
    assertThat(second.toString())
        .contains(TraceFlags.builder().setIsSampled(true).build().toString());
  }
}
