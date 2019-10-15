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

package io.opentelemetry.sdk.trace.util;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.Tracestate;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Links}. */
@RunWith(JUnit4.class)
public class LinksTest {
  private final Map<String, AttributeValue> attributesMap = new HashMap<>();
  private final Tracestate tracestate = Tracestate.builder().build();
  private final SpanContext spanContext =
      SpanContext.create(
          TestUtils.generateRandomTraceId(),
          TestUtils.generateRandomSpanId(),
          TraceFlags.getDefault(),
          tracestate);

  @Before
  public void setUp() {
    attributesMap.put("MyAttributeKey0", AttributeValue.stringAttributeValue("MyStringAttribute"));
    attributesMap.put("MyAttributeKey1", AttributeValue.longAttributeValue(10));
    attributesMap.put("MyAttributeKey2", AttributeValue.booleanAttributeValue(true));
  }

  @Test
  public void create() {
    Link link = Links.create(spanContext, attributesMap);
    assertThat(link.getContext()).isEqualTo(spanContext);
    assertThat(link.getAttributes()).isEqualTo(attributesMap);
  }

  @Test
  public void create_NoAttributes() {
    Link link = Links.create(spanContext);
    assertThat(link.getContext()).isEqualTo(spanContext);
    assertThat(link.getAttributes()).isEmpty();
  }

  @Test
  public void link_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(Links.create(spanContext), Links.create(spanContext))
        .addEqualityGroup(
            Links.create(spanContext, attributesMap), Links.create(spanContext, attributesMap));
    tester.testEquals();
  }

  @Test
  public void link_ToString() {
    Link link = Links.create(spanContext, attributesMap);
    assertThat(link.toString()).contains(spanContext.getTraceId().toString());
    assertThat(link.toString()).contains(spanContext.getSpanId().toString());
    assertThat(link.toString()).contains(attributesMap.toString());
  }
}
