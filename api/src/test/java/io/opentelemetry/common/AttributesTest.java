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

package io.opentelemetry.common;

import static com.google.common.truth.Truth.assertThat;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class AttributesTest {

  @Test
  public void testIteration() {
    Set<String> keysSeen = new HashSet<>();

    Attributes attributes =
        Attributes.of(
            "key1", AttributeValue.stringAttributeValue("value1"),
            "key2", AttributeValue.longAttributeValue(333));

    for (Entry<String, AttributeValue> attribute : attributes) {
      keysSeen.add(attribute.getKey());
    }

    assertThat(keysSeen).containsExactly("key1", "key2");
  }

  @Test
  public void testIteration_singleAttribute() {
    Set<String> keysSeen = new HashSet<>();

    Attributes attributes = Attributes.of("key", AttributeValue.stringAttributeValue("value"));

    for (Entry<String, AttributeValue> attribute : attributes) {
      keysSeen.add(attribute.getKey());
    }

    assertThat(keysSeen).containsExactly("key");
  }

  @Test
  public void testIteration_empty() {
    AtomicBoolean sawSomething = new AtomicBoolean(false);
    Attributes emptyAttributes = Attributes.empty();
    for (Entry<String, AttributeValue> unused : emptyAttributes) {
      sawSomething.set(true);
    }
    assertThat(sawSomething.get()).isFalse();
  }
}
