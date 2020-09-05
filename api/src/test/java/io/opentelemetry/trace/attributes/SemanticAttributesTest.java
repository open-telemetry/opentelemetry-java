/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace.attributes;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.trace.Span;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit tests for {@link SemanticAttributes}. */
class SemanticAttributesTest {

  private Span span;
  private Span.Builder builder;

  @BeforeEach
  void setUp() {
    span = Mockito.mock(Span.class);
    builder = Mockito.mock(Span.Builder.class);
  }

  @Test
  void shouldEnableSetAttributeOnSpan() throws IllegalAccessException {
    Field[] fields = SemanticAttributes.class.getFields();
    for (Field field : fields) {
      Object attribute = field.get(null);
      if (attribute instanceof StringAttributeSetter) {
        setAndVerify((StringAttributeSetter) attribute, span, "TestValue");
        setAndVerify((StringAttributeSetter) attribute, builder, "TestValue");
        setAndVerify((StringAttributeSetter) attribute, span, null);
        setAndVerify((StringAttributeSetter) attribute, builder, null);
      } else if (attribute instanceof LongAttributeSetter) {
        setAndVerify((LongAttributeSetter) attribute, span, 42L);
        setAndVerify((LongAttributeSetter) attribute, builder, 42L);
      } else if (attribute instanceof DoubleAttributeSetter) {
        setAndVerify((DoubleAttributeSetter) attribute, span, 3.14);
        setAndVerify((DoubleAttributeSetter) attribute, builder, 3.14);
      } else if (attribute instanceof BooleanAttributeSetter) {
        setAndVerify((BooleanAttributeSetter) attribute, span, true);
        setAndVerify((BooleanAttributeSetter) attribute, builder, true);
      }
    }
  }

  private static void setAndVerify(StringAttributeSetter setter, Span span, String value) {
    setter.set(span, value);
    Mockito.verify(span).setAttribute(setter.key(), value);
  }

  private static void setAndVerify(
      StringAttributeSetter setter, Span.Builder spanBuilder, String value) {
    setter.set(spanBuilder, value);
    Mockito.verify(spanBuilder).setAttribute(setter.key(), value);
  }

  private static void setAndVerify(LongAttributeSetter setter, Span span, long value) {
    setter.set(span, value);
    Mockito.verify(span).setAttribute(setter.key(), value);
  }

  private static void setAndVerify(
      LongAttributeSetter setter, Span.Builder spanBuilder, long value) {
    setter.set(spanBuilder, value);
    Mockito.verify(spanBuilder).setAttribute(setter.key(), value);
  }

  private static void setAndVerify(DoubleAttributeSetter setter, Span span, double value) {
    setter.set(span, value);
    Mockito.verify(span).setAttribute(setter.key(), value);
  }

  private static void setAndVerify(
      DoubleAttributeSetter setter, Span.Builder spanBuilder, double value) {
    setter.set(spanBuilder, value);
    Mockito.verify(spanBuilder).setAttribute(setter.key(), value);
  }

  private static void setAndVerify(BooleanAttributeSetter setter, Span span, boolean value) {
    setter.set(span, value);
    Mockito.verify(span).setAttribute(setter.key(), value);
  }

  private static void setAndVerify(
      BooleanAttributeSetter setter, Span.Builder spanBuilder, boolean value) {
    setter.set(spanBuilder, value);
    Mockito.verify(spanBuilder).setAttribute(setter.key(), value);
  }

  @Test
  void shouldCreateAllSetterTypes() {
    assertThat(BooleanAttributeSetter.create("attr.one"))
        .isInstanceOf(BooleanAttributeSetter.class);
    assertThat(DoubleAttributeSetter.create("attr.two")).isInstanceOf(DoubleAttributeSetter.class);
    assertThat(LongAttributeSetter.create("attr.three")).isInstanceOf(LongAttributeSetter.class);
    assertThat(StringAttributeSetter.create("attr.four")).isInstanceOf(StringAttributeSetter.class);
  }
}
