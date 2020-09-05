/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class AttributeValueTest {

  @Test
  void attributeValue_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        AttributeValue.stringAttributeValue("MyStringAttributeValue"),
        AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    tester.addEqualityGroup(AttributeValue.stringAttributeValue("MyStringAttributeDiffValue"));
    tester.addEqualityGroup(
        AttributeValue.booleanAttributeValue(true), AttributeValue.booleanAttributeValue(true));
    tester.addEqualityGroup(AttributeValue.booleanAttributeValue(false));
    tester.addEqualityGroup(
        AttributeValue.longAttributeValue(123456L), AttributeValue.longAttributeValue(123456L));
    tester.addEqualityGroup(AttributeValue.longAttributeValue(1234567L));
    tester.addEqualityGroup(
        AttributeValue.doubleAttributeValue(1.23456), AttributeValue.doubleAttributeValue(1.23456));
    tester.addEqualityGroup(AttributeValue.doubleAttributeValue(1.234567));
    tester.addEqualityGroup(
        AttributeValue.arrayAttributeValue(
            "MyArrayStringAttributeValue1", "MyArrayStringAttributeValue2"),
        AttributeValue.arrayAttributeValue(
            "MyArrayStringAttributeValue1", "MyArrayStringAttributeValue2"));
    tester.addEqualityGroup(AttributeValue.arrayAttributeValue("MyArrayStringAttributeDiffValue"));
    tester.addEqualityGroup(
        AttributeValue.arrayAttributeValue(true, false, true),
        AttributeValue.arrayAttributeValue(true, false, true));
    tester.addEqualityGroup(AttributeValue.arrayAttributeValue(false));
    tester.addEqualityGroup(
        AttributeValue.arrayAttributeValue(123456L, 7890L),
        AttributeValue.arrayAttributeValue(123456L, 7890L));
    tester.addEqualityGroup(AttributeValue.arrayAttributeValue(1234567L));
    tester.addEqualityGroup(
        AttributeValue.arrayAttributeValue(1.23456, 7.890),
        AttributeValue.arrayAttributeValue(1.23456, 7.890));
    tester.addEqualityGroup(AttributeValue.arrayAttributeValue(1.234567));
    tester.testEquals();
  }

  @Test
  void doNotCrashOnNull() {
    AttributeValue.stringAttributeValue(null);
    AttributeValue.arrayAttributeValue((String[]) null);
    AttributeValue.arrayAttributeValue((Boolean[]) null);
    AttributeValue.arrayAttributeValue((Long[]) null);
    AttributeValue.arrayAttributeValue((Double[]) null);
  }

  @Test
  void attributeValue_ToString() {
    AttributeValue attribute = AttributeValue.stringAttributeValue("MyStringAttributeValue");
    assertThat(attribute.toString()).contains("MyStringAttributeValue");
    attribute = AttributeValue.booleanAttributeValue(true);
    assertThat(attribute.toString()).contains("true");
    attribute = AttributeValue.longAttributeValue(123456L);
    assertThat(attribute.toString()).contains("123456");
    attribute = AttributeValue.doubleAttributeValue(1.23456);
    assertThat(attribute.toString()).contains("1.23456");
    attribute =
        AttributeValue.arrayAttributeValue(
            "MyArrayStringAttributeValue1", "MyArrayStringAttributeValue2");
    assertThat(attribute.toString()).contains("MyArrayStringAttributeValue1");
    assertThat(attribute.toString()).contains("MyArrayStringAttributeValue2");
    attribute = AttributeValue.arrayAttributeValue(true, false);
    assertThat(attribute.toString()).contains("true");
    assertThat(attribute.toString()).contains("false");
    attribute = AttributeValue.arrayAttributeValue(12345L, 67890L);
    assertThat(attribute.toString()).contains("12345");
    assertThat(attribute.toString()).contains("67890");
    attribute = AttributeValue.arrayAttributeValue(1.2345, 6.789);
    assertThat(attribute.toString()).contains("1.2345");
    assertThat(attribute.toString()).contains("6.789");
  }

  @Test
  void arrayAttributeValue_nullValuesWithinArray() {
    AttributeValue attribute;

    attribute = AttributeValue.arrayAttributeValue("string", null, "", "string");
    assertThat(attribute.getStringArrayValue().size()).isEqualTo(4);

    attribute = AttributeValue.arrayAttributeValue(10L, null, 20L);
    assertThat(attribute.getLongArrayValue().size()).isEqualTo(3);

    attribute = AttributeValue.arrayAttributeValue(true, null, false);
    assertThat(attribute.getBooleanArrayValue().size()).isEqualTo(3);

    attribute = AttributeValue.arrayAttributeValue(1.2, null, 3.4);
    assertThat(attribute.getDoubleArrayValue().size()).isEqualTo(3);
  }
}
