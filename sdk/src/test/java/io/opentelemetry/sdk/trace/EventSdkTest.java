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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.trace.AttributeValue;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link EventSdk}. */
@RunWith(JUnit4.class)
public class EventSdkTest {

  private static final String NAME = "event";
  private static final Map<String, AttributeValue> ATTRIBUTES =
      Collections.singletonMap("attribute", AttributeValue.stringAttributeValue("value"));

  @Test
  public void createWithName() {
    EventSdk event = EventSdk.create(NAME);
    assertThat(event.getName()).isEqualTo(NAME);
    assertThat(event.getAttributes()).isEmpty();
  }

  @Test
  public void createAndGet() {
    EventSdk event = EventSdk.create(NAME, ATTRIBUTES);
    assertThat(event.getName()).isEqualTo(NAME);
    assertThat(event.getAttributes()).isEqualTo(ATTRIBUTES);
  }
}
