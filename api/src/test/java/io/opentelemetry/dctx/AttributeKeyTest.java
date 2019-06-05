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

/** Tests for {@link AttributeKey}. */
@RunWith(JUnit4.class)
public final class AttributeKeyTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testMaxLength() {
    assertThat(AttributeKey.MAX_LENGTH).isEqualTo(255);
  }

  @Test
  public void testGetName() {
    assertThat(AttributeKey.create("foo").getName()).isEqualTo("foo");
  }

  @Test
  public void create_AllowAttributeKeyNameWithMaxLength() {
    char[] chars = new char[AttributeKey.MAX_LENGTH];
    Arrays.fill(chars, 'k');
    String key = new String(chars);
    assertThat(AttributeKey.create(key).getName()).isEqualTo(key);
  }

  @Test
  public void create_DisallowAttributeKeyNameOverMaxLength() {
    char[] chars = new char[AttributeKey.MAX_LENGTH + 1];
    Arrays.fill(chars, 'k');
    String key = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    AttributeKey.create(key);
  }

  @Test
  public void create_DisallowUnprintableChars() {
    thrown.expect(IllegalArgumentException.class);
    AttributeKey.create("\2ab\3cd");
  }

  @Test
  public void createString_DisallowEmpty() {
    thrown.expect(IllegalArgumentException.class);
    AttributeKey.create("");
  }

  @Test
  public void testAttributeKeyEquals() {
    new EqualsTester()
        .addEqualityGroup(AttributeKey.create("foo"), AttributeKey.create("foo"))
        .addEqualityGroup(AttributeKey.create("bar"))
        .testEquals();
  }
}
