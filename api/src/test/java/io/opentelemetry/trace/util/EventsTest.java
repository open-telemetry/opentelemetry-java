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

package io.opentelemetry.trace.util;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Events}. */
@RunWith(JUnit4.class)
public class EventsTest {
  private final Map<String, AttributeValue> attributesMap = new HashMap<>();

  @Before
  public void setUp() {
    attributesMap.put("MyAttributeKey0", AttributeValue.stringAttributeValue("MyStringAttribute"));
    attributesMap.put("MyAttributeKey1", AttributeValue.longAttributeValue(10));
    attributesMap.put("MyAttributeKey2", AttributeValue.booleanAttributeValue(true));
  }

  @Test
  public void create() {
    Event event = Events.create("test", attributesMap);
    assertThat(event.getName()).isEqualTo("test");
    assertThat(event.getAttributes()).isEqualTo(attributesMap);
  }

  @Test
  public void create_NoAttributes() {
    Event event = Events.create("test");
    assertThat(event.getName()).isEqualTo("test");
    assertThat(event.getAttributes()).isEmpty();
  }

  @Test
  public void link_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(Events.create("test"), Events.create("test"))
        .addEqualityGroup(
            Events.create("test", attributesMap), Events.create("test", attributesMap));
    tester.testEquals();
  }

  @Test
  public void link_ToString() {
    Event event = Events.create("test", attributesMap);
    assertThat(event.toString()).contains("test");
    assertThat(event.toString()).contains(attributesMap.toString());
  }
}
