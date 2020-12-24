/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2018, OpenCensus Authors
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

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;

import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class OpenTelemetryNoRecordEventsSpanImplTest {
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.DEFAULT,
          Tracestate.builder().build());
  private final OpenTelemetryNoRecordEventsSpanImpl noRecordEventsSpan =
      OpenTelemetryNoRecordEventsSpanImpl.create(spanContext);

  @Test
  public void propagatesSpanContext() {
    assertThat(noRecordEventsSpan.getContext()).isEqualTo(spanContext);
    assertThat(noRecordEventsSpan.getSpanContext())
        .isEqualTo(SpanConverter.mapSpanContext(spanContext));
  }

  @Test
  public void isNotRecording() {
    assertThat(noRecordEventsSpan.getOptions()).doesNotContain(Options.RECORD_EVENTS);
    assertThat(noRecordEventsSpan.isRecording()).isFalse();
  }

  @Test
  public void doNotCrash() {
    Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    Map<String, AttributeValue> multipleAttributes = new HashMap<String, AttributeValue>();
    multipleAttributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    multipleAttributes.put("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true));
    multipleAttributes.put("MyLongAttributeKey", AttributeValue.longAttributeValue(123));
    // Tests only that all the methods are not crashing/throwing errors.
    noRecordEventsSpan.putAttribute(
        "MyStringAttributeKey2", AttributeValue.stringAttributeValue("MyStringAttributeValue2"));
    noRecordEventsSpan.addAnnotation("MyAnnotation");
    noRecordEventsSpan.addAnnotation("MyAnnotation", attributes);
    noRecordEventsSpan.addAnnotation("MyAnnotation", multipleAttributes);
    noRecordEventsSpan.addAnnotation(Annotation.fromDescription("MyAnnotation"));
    noRecordEventsSpan.addMessageEvent(MessageEvent.builder(MessageEvent.Type.SENT, 1L).build());
    noRecordEventsSpan.addLink(
        Link.fromSpanContext(SpanContext.INVALID, Link.Type.CHILD_LINKED_SPAN));
    noRecordEventsSpan.setStatus(Status.OK);
    noRecordEventsSpan.setAttribute("OTelAttributeKeyString", "OTelAttributeValue");
    noRecordEventsSpan.setAttribute("OTelAttributeKeyLong", 123);
    noRecordEventsSpan.setAttribute("OTelAttributeKeyDouble", 123.45);
    noRecordEventsSpan.setAttribute("OTelAttributeKeyBoolean", true);
    noRecordEventsSpan.addEvent("OTel event 1");
    noRecordEventsSpan.addEvent("OTel event 2", 29922310, TimeUnit.HOURS);
    noRecordEventsSpan.addEvent(
        "OTel event 2", Attributes.of(AttributeKey.booleanKey("OTelAttributeKey"), true));
    noRecordEventsSpan.updateName("OTel name");
    noRecordEventsSpan.end(EndSpanOptions.DEFAULT);
    noRecordEventsSpan.end();
  }
}
