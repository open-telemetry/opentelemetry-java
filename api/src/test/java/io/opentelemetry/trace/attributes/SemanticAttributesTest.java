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

package io.opentelemetry.trace.attributes;

import static org.junit.Assert.assertEquals;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SemanticAttributes}. */
@RunWith(JUnit4.class)
public class SemanticAttributesTest {

  private Span span;

  @Before
  public void setUp() {
    Tracer tracer = OpenTelemetry.getTracerProvider().get("io.telemetry.api");
    span = tracer.spanBuilder("junit").startSpan();
  }

  @Test
  public void shouldEnableSetAttributeOnSpan() throws IllegalAccessException {
    Set<String> keys = new HashSet<>();
    Field[] fields = SemanticAttributes.class.getFields();
    for (int i = 0; i < fields.length; i++) {
      AttributeSetter<?> attribute = (AttributeSetter<?>) fields[i].get(null);
      keys.add(attribute.key());
      if (attribute instanceof StringAttributeSetter) {
        ((StringAttributeSetter) attribute).set(span, "TestValue");
        ((StringAttributeSetter) attribute).set(span, null);
      } else if (attribute instanceof IntOrStringAttributeSetter) {
        ((IntOrStringAttributeSetter) attribute).set(span, 80);
        ((IntOrStringAttributeSetter) attribute).set(span, "443");
        ((IntOrStringAttributeSetter) attribute).set(span, (String) null);
      } else if (attribute instanceof LongAttributeSetter) {
        ((LongAttributeSetter) attribute).set(span, 42L);
        ((LongAttributeSetter) attribute).set(span, null);
      } else if (attribute instanceof DoubleAttributeSetter) {
        ((DoubleAttributeSetter) attribute).set(span, 3.14);
        ((DoubleAttributeSetter) attribute).set(span, null);
      } else if (attribute instanceof BooleanAttributeSetter) {
        ((BooleanAttributeSetter) attribute).set(span, false);
        ((BooleanAttributeSetter) attribute).set(span, null);
      }
    }
    assertEquals(fields.length, keys.size());
  }
}
