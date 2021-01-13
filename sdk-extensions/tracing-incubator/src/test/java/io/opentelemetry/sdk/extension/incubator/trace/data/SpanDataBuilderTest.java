/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.junit.jupiter.api.Test;

class SpanDataBuilderTest {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  private static final TestSpanData TEST_SPAN_DATA =
      TestSpanData.builder()
          .setHasEnded(true)
          .setTraceId(TRACE_ID)
          .setSpanId(SPAN_ID)
          .setName("GET /api/endpoint")
          .setStartEpochNanos(0)
          .setEndEpochNanos(100)
          .setKind(Span.Kind.SERVER)
          .setStatus(StatusData.error())
          .setAttributes(Attributes.builder().put("cat", "meow").put("dog", "bark").build())
          .setTotalRecordedEvents(1000)
          .setTotalRecordedLinks(2300)
          .build();

  @Test
  void noOp() {
    assertThat(SpanDataBuilder.builder(TEST_SPAN_DATA).build())
        .usingRecursiveComparison()
        .isEqualTo(TEST_SPAN_DATA);
  }

  @Test
  void modifySpanData() {
    assertThat(TEST_SPAN_DATA.getStatus()).isEqualTo(StatusData.error());
    SpanData modified =
        SpanDataBuilder.builder(TEST_SPAN_DATA)
            .setStatus(StatusData.create(StatusCode.ERROR, "ABORTED"))
            .build();
    assertThat(modified.getStatus()).isEqualTo(StatusData.create(StatusCode.ERROR, "ABORTED"));
  }

  @Test
  void equalsHashCode() {
    assertThat(SpanDataBuilder.builder(TEST_SPAN_DATA).build()).isEqualTo(TEST_SPAN_DATA);
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(
            SpanDataBuilder.builder(TEST_SPAN_DATA).build(),
            SpanDataBuilder.builder(TEST_SPAN_DATA).build())
        .addEqualityGroup(
            SpanDataBuilder.builder(TEST_SPAN_DATA)
                .setStatus(StatusData.create(StatusCode.ERROR, "ABORTED"))
                .build());
    tester.testEquals();
  }
}
