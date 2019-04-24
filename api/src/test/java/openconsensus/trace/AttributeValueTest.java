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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AttributeValue}. */
@RunWith(JUnit4.class)
public class AttributeValueTest {

  @Test
  public void attributeValue_EqualsAndHashCode() {
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
    tester.testEquals();
  }

  @Test
  public void attributeValue_ToString() {
    AttributeValue attribute = AttributeValue.stringAttributeValue("MyStringAttributeValue");
    assertThat(attribute.toString()).contains("MyStringAttributeValue");
    attribute = AttributeValue.booleanAttributeValue(true);
    assertThat(attribute.toString()).contains("true");
    attribute = AttributeValue.longAttributeValue(123456L);
    assertThat(attribute.toString()).contains("123456");
    attribute = AttributeValue.doubleAttributeValue(1.23456);
    assertThat(attribute.toString()).contains("1.23456");
  }
}
