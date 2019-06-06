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
import io.opentelemetry.distributedcontext.AttributeMetadata.AttributeTtl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Attribute}. */
@RunWith(JUnit4.class)
public final class AttributeTest {

  private static final AttributeKey KEY = AttributeKey.create("KEY");
  private static final AttributeKey KEY_2 = AttributeKey.create("KEY2");
  private static final AttributeValue VALUE = AttributeValue.create("VALUE");
  private static final AttributeValue VALUE_2 = AttributeValue.create("VALUE2");
  private static final AttributeMetadata METADATA_UNLIMITED_PROPAGATION =
      AttributeMetadata.create(AttributeTtl.UNLIMITED_PROPAGATION);
  private static final AttributeMetadata METADATA_NO_PROPAGATION =
      AttributeMetadata.create(AttributeTtl.NO_PROPAGATION);

  @Test
  public void testGetKey() {
    assertThat(Attribute.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION).getKey())
        .isEqualTo(KEY);
  }

  @Test
  public void testGetAttributeMetadata() {
    assertThat(Attribute.create(KEY, VALUE, METADATA_NO_PROPAGATION).getAttributeMetadata())
        .isEqualTo(METADATA_NO_PROPAGATION);
  }

  @Test
  public void testAttributeEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Attribute.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION),
            Attribute.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Attribute.create(KEY, VALUE_2, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Attribute.create(KEY_2, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Attribute.create(KEY, VALUE, METADATA_NO_PROPAGATION))
        .testEquals();
  }
}
