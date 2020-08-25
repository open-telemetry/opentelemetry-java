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

package io.opentelemetry.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class AttributeValueTest {

  @Test
  void attributeValue_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        AttributeValue.Factory.stringAttributeValue("MyStringAttributeValue"),
        AttributeValue.Factory.stringAttributeValue("MyStringAttributeValue"));
    tester.addEqualityGroup(
        AttributeValue.Factory.stringAttributeValue("MyStringAttributeDiffValue"));
    tester.addEqualityGroup(
        AttributeValue.Factory.booleanAttributeValue(true),
        AttributeValue.Factory.booleanAttributeValue(true));
    tester.addEqualityGroup(AttributeValue.Factory.booleanAttributeValue(false));
    tester.addEqualityGroup(
        AttributeValue.Factory.longAttributeValue(123456L),
        AttributeValue.Factory.longAttributeValue(123456L));
    tester.addEqualityGroup(AttributeValue.Factory.longAttributeValue(1234567L));
    tester.addEqualityGroup(
        AttributeValue.Factory.doubleAttributeValue(1.23456),
        AttributeValue.Factory.doubleAttributeValue(1.23456));
    tester.addEqualityGroup(AttributeValue.Factory.doubleAttributeValue(1.234567));
    tester.addEqualityGroup(
        AttributeValue.Factory.arrayAttributeValue(
            "MyArrayStringAttributeValue1", "MyArrayStringAttributeValue2"),
        AttributeValue.Factory.arrayAttributeValue(
            "MyArrayStringAttributeValue1", "MyArrayStringAttributeValue2"));
    tester.addEqualityGroup(
        AttributeValue.Factory.arrayAttributeValue("MyArrayStringAttributeDiffValue"));
    tester.addEqualityGroup(
        AttributeValue.Factory.arrayAttributeValue(true, false, true),
        AttributeValue.Factory.arrayAttributeValue(true, false, true));
    tester.addEqualityGroup(AttributeValue.Factory.arrayAttributeValue(false));
    tester.addEqualityGroup(
        AttributeValue.Factory.arrayAttributeValue(123456L, 7890L),
        AttributeValue.Factory.arrayAttributeValue(123456L, 7890L));
    tester.addEqualityGroup(AttributeValue.Factory.arrayAttributeValue(1234567L));
    tester.addEqualityGroup(
        AttributeValue.Factory.arrayAttributeValue(1.23456, 7.890),
        AttributeValue.Factory.arrayAttributeValue(1.23456, 7.890));
    tester.addEqualityGroup(AttributeValue.Factory.arrayAttributeValue(1.234567));
    tester.testEquals();
  }

  @Test
  void doNotCrashOnNull() {
    AttributeValue.Factory.stringAttributeValue(null);
    AttributeValue.Factory.arrayAttributeValue((String[]) null);
    AttributeValue.Factory.arrayAttributeValue((Boolean[]) null);
    AttributeValue.Factory.arrayAttributeValue((Long[]) null);
    AttributeValue.Factory.arrayAttributeValue((Double[]) null);
  }

  @Test
  void attributeValue_ToString() {
    AttributeValue attribute =
        AttributeValue.Factory.stringAttributeValue("MyStringAttributeValue");
    assertThat(attribute.toString()).contains("MyStringAttributeValue");
    attribute = AttributeValue.Factory.booleanAttributeValue(true);
    assertThat(attribute.toString()).contains("true");
    attribute = AttributeValue.Factory.longAttributeValue(123456L);
    assertThat(attribute.toString()).contains("123456");
    attribute = AttributeValue.Factory.doubleAttributeValue(1.23456);
    assertThat(attribute.toString()).contains("1.23456");
    attribute =
        AttributeValue.Factory.arrayAttributeValue(
            "MyArrayStringAttributeValue1", "MyArrayStringAttributeValue2");
    assertThat(attribute.toString()).contains("MyArrayStringAttributeValue1");
    assertThat(attribute.toString()).contains("MyArrayStringAttributeValue2");
    attribute = AttributeValue.Factory.arrayAttributeValue(true, false);
    assertThat(attribute.toString()).contains("true");
    assertThat(attribute.toString()).contains("false");
    attribute = AttributeValue.Factory.arrayAttributeValue(12345L, 67890L);
    assertThat(attribute.toString()).contains("12345");
    assertThat(attribute.toString()).contains("67890");
    attribute = AttributeValue.Factory.arrayAttributeValue(1.2345, 6.789);
    assertThat(attribute.toString()).contains("1.2345");
    assertThat(attribute.toString()).contains("6.789");
  }

  @Test
  void arrayAttributeValue_nullValuesWithinArray() {
    AttributeValue attribute;

    attribute = AttributeValue.Factory.arrayAttributeValue("string", null, "", "string");
    assertThat(attribute.getStringArrayValue().size()).isEqualTo(4);

    attribute = AttributeValue.Factory.arrayAttributeValue(10L, null, 20L);
    assertThat(attribute.getLongArrayValue().size()).isEqualTo(3);

    attribute = AttributeValue.Factory.arrayAttributeValue(true, null, false);
    assertThat(attribute.getBooleanArrayValue().size()).isEqualTo(3);

    attribute = AttributeValue.Factory.arrayAttributeValue(1.2, null, 3.4);
    assertThat(attribute.getDoubleArrayValue().size()).isEqualTo(3);
  }
}
