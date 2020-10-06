/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.incubator.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.StatusCanonicalCode;
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
          .setStatus(Status.error())
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
    assertThat(TEST_SPAN_DATA.getStatus()).isEqualTo(Status.error());
    SpanData modified =
        SpanDataBuilder.newBuilder(TEST_SPAN_DATA)
            .setStatus(Status.create(StatusCanonicalCode.ERROR, "ABORTED"))
            .build();
    assertThat(modified.getStatus()).isEqualTo(Status.create(StatusCanonicalCode.ERROR, "ABORTED"));
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
                .setStatus(Status.create(StatusCanonicalCode.ERROR, "ABORTED"))
                .build());
    tester.testEquals();
  }
}
