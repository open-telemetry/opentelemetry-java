/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.extensions.incubator.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Status;
import org.junit.jupiter.api.Test;

class SpanDataBuilderTest {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  private static final TestSpanData TEST_SPAN_DATA =
      TestSpanData.newBuilder()
          .setHasEnded(true)
          .setTraceId(TRACE_ID)
          .setSpanId(SPAN_ID)
          .setName("GET /api/endpoint")
          .setStartEpochNanos(0)
          .setEndEpochNanos(100)
          .setKind(Span.Kind.SERVER)
          .setStatus(Status.ERROR)
          .setAttributes(
              Attributes.newBuilder()
                  .setAttribute("cat", "meow")
                  .setAttribute("dog", "bark")
                  .build())
          .setTotalRecordedEvents(1000)
          .setTotalRecordedLinks(2300)
          .build();

  @Test
  void noOp() {
    assertThat(SpanDataBuilder.newBuilder(TEST_SPAN_DATA).build())
        .isEqualToComparingFieldByField(TEST_SPAN_DATA);
  }

  @Test
  void modifySpanData() {
    assertThat(TEST_SPAN_DATA.getStatus()).isEqualTo(Status.ERROR);
    SpanData modified =
        SpanDataBuilder.newBuilder(TEST_SPAN_DATA)
            .setStatus(Status.ERROR.withDescription("ABORTED"))
            .build();
    assertThat(modified.getStatus()).isEqualTo(Status.ERROR.withDescription("ABORTED"));
  }

  @Test
  void equalsHashCode() {
    assertThat(SpanDataBuilder.newBuilder(TEST_SPAN_DATA).build()).isEqualTo(TEST_SPAN_DATA);
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(
            SpanDataBuilder.newBuilder(TEST_SPAN_DATA).build(),
            SpanDataBuilder.newBuilder(TEST_SPAN_DATA).build())
        .addEqualityGroup(
            SpanDataBuilder.newBuilder(TEST_SPAN_DATA)
                .setStatus(Status.ERROR.withDescription("ABORTED"))
                .build());
    tester.testEquals();
  }
}
