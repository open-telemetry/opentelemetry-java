/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Link}. */
@RunWith(JUnit4.class)
public class LinkTest {
  private final Map<String, AttributeValue> attributesMap = new HashMap<>();
  private final Random random = new Random(1234);
  private final Tracestate tracestate = Tracestate.builder().build();
  private final SpanContext spanContext =
      SpanContext.create(
          TestUtils.generateRandomTraceId(random),
          TestUtils.generateRandomSpanId(random),
          TraceOptions.DEFAULT,
          tracestate);

  @Before
  public void setUp() {
    attributesMap.put("MyAttributeKey0", AttributeValue.stringAttributeValue("MyStringAttribute"));
    attributesMap.put("MyAttributeKey1", AttributeValue.longAttributeValue(10));
    attributesMap.put("MyAttributeKey2", AttributeValue.booleanAttributeValue(true));
  }

  @Test
  public void create() {
    Link link = Link.create(spanContext, attributesMap);
    assertThat(link.getContext()).isEqualTo(spanContext);
    assertThat(link.getAttributes()).isEqualTo(attributesMap);
  }

  @Test
  public void link_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(Link.create(spanContext), Link.create(spanContext))
        .addEqualityGroup(Link.create(SpanContext.BLANK))
        .addEqualityGroup(
            Link.create(spanContext, attributesMap), Link.create(spanContext, attributesMap));
    tester.testEquals();
  }

  @Test
  public void link_ToString() {
    Link link = Link.create(spanContext, attributesMap);
    assertThat(link.toString()).contains(spanContext.getTraceId().toString());
    assertThat(link.toString()).contains(spanContext.getSpanId().toString());
    assertThat(link.toString()).contains(attributesMap.toString());
  }
}
