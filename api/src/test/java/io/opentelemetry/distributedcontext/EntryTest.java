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

package io.opentelemetry.distributedcontext;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.distributedcontext.LabelMetadata.HopLimit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Label}. */
@RunWith(JUnit4.class)
public final class EntryTest {

  private static final LabelKey KEY = LabelKey.create("KEY");
  private static final LabelKey KEY_2 = LabelKey.create("KEY2");
  private static final LabelValue VALUE = LabelValue.create("VALUE");
  private static final LabelValue VALUE_2 = LabelValue.create("VALUE2");
  private static final LabelMetadata METADATA_UNLIMITED_PROPAGATION =
      LabelMetadata.create(HopLimit.UNLIMITED_PROPAGATION);
  private static final LabelMetadata METADATA_NO_PROPAGATION =
      LabelMetadata.create(HopLimit.NO_PROPAGATION);

  @Test
  public void testGetKey() {
    assertThat(Label.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION).getKey()).isEqualTo(KEY);
  }

  @Test
  public void testGetEntryMetadata() {
    assertThat(Label.create(KEY, VALUE, METADATA_NO_PROPAGATION).getEntryMetadata())
        .isEqualTo(METADATA_NO_PROPAGATION);
  }

  @Test
  public void testEntryEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Label.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION),
            Label.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Label.create(KEY, VALUE_2, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Label.create(KEY_2, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Label.create(KEY, VALUE, METADATA_NO_PROPAGATION))
        .testEquals();
  }
}
