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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Entry}. */
@RunWith(JUnit4.class)
public final class EntryTest {

  private static final EntryKey KEY = EntryKey.create("KEY");
  private static final EntryKey KEY_2 = EntryKey.create("KEY2");
  private static final EntryValue VALUE = EntryValue.create("VALUE");
  private static final EntryValue VALUE_2 = EntryValue.create("VALUE2");
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
}
