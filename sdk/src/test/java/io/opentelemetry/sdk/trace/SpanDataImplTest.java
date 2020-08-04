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

package io.opentelemetry.sdk.trace;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import org.junit.jupiter.api.Test;

class SpanDataImplTest {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  private static final SpanData TEST_SPAN_DATA =
      TestSpanData.newBuilder()
          .setHasEnded(true)
          .setTraceId(TraceId.fromLowerBase16(TRACE_ID, 0))
          .setSpanId(SpanId.fromLowerBase16(SPAN_ID, 0))
          .setName("GET /api/endpoint")
          .setStartEpochNanos(0)
          .setEndEpochNanos(100)
          .setKind(Span.Kind.SERVER)
          .setStatus(Status.UNKNOWN)
          .setAttributes(
              Attributes.newBuilder()
                  .setAttribute("cat", "meow")
                  .setAttribute("dog", "bark")
                  .build())
          .setTotalRecordedEvents(1000)
          .setTotalRecordedLinks(2300)
          .build();

  @Test
  void equalsHashCode() {
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(TEST_SPAN_DATA, SpanDataImpl.newBuilder(TEST_SPAN_DATA).build())
        .addEqualityGroup(TEST_SPAN_DATA.toBuilder().setStatus(Status.ABORTED).build());
    tester.testEquals();
  }
}
