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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanContext}. */
class SpanContextTest {
  private static final byte[] firstTraceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondTraceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0, 0, 0, 0, 0};
  private static final byte[] firstSpanIdBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondSpanIdBytes = new byte[] {'0', 0, 0, 0, 0, 0, 0, 0};
  private static final TraceState FIRST_TRACE_STATE =
      TraceState.builder().set("foo", "bar").build();
  private static final TraceState SECOND_TRACE_STATE =
      TraceState.builder().set("foo", "baz").build();
  private static final TraceState EMPTY_TRACE_STATE = TraceState.builder().build();
  private static final SpanContext first =
      SpanContext.create(
          TraceId.fromBytes(firstTraceIdBytes, 0),
          SpanId.fromBytes(firstSpanIdBytes, 0),
          TraceFlags.getDefault(),
          FIRST_TRACE_STATE);
  private static final SpanContext second =
      SpanContext.create(
          TraceId.fromBytes(secondTraceIdBytes, 0),
          SpanId.fromBytes(secondSpanIdBytes, 0),
          TraceFlags.builder().setIsSampled(true).build(),
          SECOND_TRACE_STATE);
  private static final SpanContext remote =
      SpanContext.createFromRemoteParent(
          TraceId.fromBytes(secondTraceIdBytes, 0),
          SpanId.fromBytes(secondSpanIdBytes, 0),
          TraceFlags.builder().setIsSampled(true).build(),
          EMPTY_TRACE_STATE);

  @Test
  void invalidSpanContext() {
    assertThat(SpanContext.getInvalid().getTraceId()).isEqualTo(TraceId.getInvalid());
    assertThat(SpanContext.getInvalid().getSpanId()).isEqualTo(SpanId.getInvalid());
    assertThat(SpanContext.getInvalid().getTraceFlags()).isEqualTo(TraceFlags.getDefault());
  }

  @Test
  void isValid() {
    assertThat(SpanContext.getInvalid().isValid()).isFalse();
    assertThat(
            SpanContext.create(
                    TraceId.fromBytes(firstTraceIdBytes, 0),
                    SpanId.getInvalid(),
                    TraceFlags.getDefault(),
                    EMPTY_TRACE_STATE)
                .isValid())
        .isFalse();
    assertThat(
            SpanContext.create(
                    TraceId.getInvalid(),
                    SpanId.fromBytes(firstSpanIdBytes, 0),
                    TraceFlags.getDefault(),
                    EMPTY_TRACE_STATE)
                .isValid())
        .isFalse();
    assertThat(first.isValid()).isTrue();
    assertThat(second.isValid()).isTrue();
  }

  @Test
  void getTraceId() {
    assertThat(first.getTraceId()).isEqualTo(TraceId.fromBytes(firstTraceIdBytes, 0));
    assertThat(second.getTraceId()).isEqualTo(TraceId.fromBytes(secondTraceIdBytes, 0));
  }

  @Test
  void getSpanId() {
    assertThat(first.getSpanId()).isEqualTo(SpanId.fromBytes(firstSpanIdBytes, 0));
    assertThat(second.getSpanId()).isEqualTo(SpanId.fromBytes(secondSpanIdBytes, 0));
  }

  @Test
  void getTraceFlags() {
    assertThat(first.getTraceFlags()).isEqualTo(TraceFlags.getDefault());
    assertThat(second.getTraceFlags()).isEqualTo(TraceFlags.builder().setIsSampled(true).build());
  }

  @Test
  void getTraceState() {
    assertThat(first.getTraceState()).isEqualTo(FIRST_TRACE_STATE);
    assertThat(second.getTraceState()).isEqualTo(SECOND_TRACE_STATE);
  }

  @Test
  void isRemote() {
    assertThat(first.isRemote()).isFalse();
    assertThat(second.isRemote()).isFalse();
    assertThat(remote.isRemote()).isTrue();
  }
}
