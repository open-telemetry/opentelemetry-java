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

package io.opentelemetry.correlationcontext;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link EntryValue}. */
@RunWith(JUnit4.class)
public final class EntryValueTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testMaxLength() {
    assertThat(EntryValue.MAX_LENGTH).isEqualTo(255);
  }

  @Test
  public void testAsString() {
    assertThat(EntryValue.create("foo").asString()).isEqualTo("foo");
  }

  @Test
  public void create_AllowEntryValueWithMaxLength() {
    char[] chars = new char[EntryValue.MAX_LENGTH];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    assertThat(EntryValue.create(value).asString()).isEqualTo(value);
  }

  @Test
  public void create_DisallowEntryValueOverMaxLength() {
    char[] chars = new char[EntryValue.MAX_LENGTH + 1];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    EntryValue.create(value);
  }

  @Test
  public void disallowEntryValueWithUnprintableChars() {
    String value = "\2ab\3cd";
    thrown.expect(IllegalArgumentException.class);
    EntryValue.create(value);
  }

  @Test
  public void testEntryValueEquals() {
    new EqualsTester()
        .addEqualityGroup(EntryValue.create("foo"), EntryValue.create("foo"))
        .addEqualityGroup(EntryValue.create("bar"))
        .testEquals();
  }
}
