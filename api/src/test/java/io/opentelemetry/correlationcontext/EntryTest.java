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
import io.opentelemetry.correlationcontext.EntryMetadata.EntryTtl;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Entry}. */
@RunWith(JUnit4.class)
public final class EntryTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String KEY = "KEY";
  private static final String KEY_2 = "KEY2";
  private static final String VALUE = "VALUE";
  private static final String VALUE_2 = "VALUE2";
  private static final EntryMetadata METADATA_UNLIMITED_PROPAGATION =
      EntryMetadata.create(EntryTtl.UNLIMITED_PROPAGATION);
  private static final EntryMetadata METADATA_NO_PROPAGATION =
      EntryMetadata.create(EntryTtl.NO_PROPAGATION);

  @Test
  public void testGetKey() {
    assertThat(Entry.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION).getKey()).isEqualTo(KEY);
  }

  @Test
  public void testGetEntryMetadata() {
    assertThat(Entry.create(KEY, VALUE, METADATA_NO_PROPAGATION).getEntryMetadata())
        .isEqualTo(METADATA_NO_PROPAGATION);
  }

  @Test
  public void testEntryEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Entry.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION),
            Entry.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Entry.create(KEY, VALUE_2, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Entry.create(KEY_2, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Entry.create(KEY, VALUE, METADATA_NO_PROPAGATION))
        .testEquals();
  }

  @Test
  public void testKeyMaxLength() {
    assertThat(Entry.MAX_KEY_LENGTH).isEqualTo(255);
  }

  @Test
  public void create_AllowEntryKeyNameWithMaxLength() {
    char[] chars = new char[Entry.MAX_KEY_LENGTH];
    Arrays.fill(chars, 'k');
    String key = new String(chars);
    assertThat(Entry.create(key, "value", Entry.METADATA_UNLIMITED_PROPAGATION)).isNotNull();
  }

  @Test
  public void create_DisallowEntryKeyNameOverMaxLength() {
    char[] chars = new char[Entry.MAX_KEY_LENGTH + 1];
    Arrays.fill(chars, 'k');
    String key = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    Entry.create(key, "value", Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void create_DisallowKeyUnprintableChars() {
    thrown.expect(IllegalArgumentException.class);
    Entry.create("\2ab\3cd", "value", Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void createString_DisallowKeyEmpty() {
    thrown.expect(IllegalArgumentException.class);
    Entry.create("", "value", Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void testValueMaxLength() {
    assertThat(Entry.MAX_VALUE_LENGTH).isEqualTo(255);
  }

  @Test
  public void create_AllowEntryValueWithMaxLength() {
    char[] chars = new char[Entry.MAX_VALUE_LENGTH];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    assertThat(Entry.create("key", value, Entry.METADATA_UNLIMITED_PROPAGATION).getValue())
        .isEqualTo(value);
  }

  @Test
  public void create_DisallowEntryValueOverMaxLength() {
    char[] chars = new char[Entry.MAX_VALUE_LENGTH + 1];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    Entry.create("key", value, Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void disallowEntryValueWithUnprintableChars() {
    String value = "\2ab\3cd";
    thrown.expect(IllegalArgumentException.class);
    Entry.create("key", value, Entry.METADATA_UNLIMITED_PROPAGATION);
  }
}
