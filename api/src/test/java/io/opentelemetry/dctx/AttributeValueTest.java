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

package io.opentelemetry.dctx;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link AttributeValue}. */
@RunWith(JUnit4.class)
public final class AttributeValueTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testMaxLength() {
    assertThat(AttributeValue.MAX_LENGTH).isEqualTo(255);
  }

  @Test
  public void testAsString() {
    assertThat(AttributeValue.create("foo").asString()).isEqualTo("foo");
  }

  @Test
  public void create_AllowAttributeValueWithMaxLength() {
    char[] chars = new char[AttributeValue.MAX_LENGTH];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    assertThat(AttributeValue.create(value).asString()).isEqualTo(value);
  }

  @Test
  public void create_DisallowAttributeValueOverMaxLength() {
    char[] chars = new char[AttributeValue.MAX_LENGTH + 1];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    AttributeValue.create(value);
  }

  @Test
  public void disallowAttributeValueWithUnprintableChars() {
    String value = "\2ab\3cd";
    thrown.expect(IllegalArgumentException.class);
    AttributeValue.create(value);
  }

  @Test
  public void testAttributeValueEquals() {
    new EqualsTester()
        .addEqualityGroup(AttributeValue.create("foo"), AttributeValue.create("foo"))
        .addEqualityGroup(AttributeValue.create("bar"))
        .testEquals();
  }
}
