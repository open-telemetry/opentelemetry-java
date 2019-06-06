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

/** Tests for {@link AttributeMetadata}. */
@RunWith(JUnit4.class)
public class AttributeMetadataTest {

  @Test
  public void testGetAttributeTtl() {
    AttributeMetadata attrMetadata = AttributeMetadata.create(AttributeTtl.NO_PROPAGATION);
    assertThat(attrMetadata.getAttributeTtl()).isEqualTo(AttributeTtl.NO_PROPAGATION);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            AttributeMetadata.create(AttributeTtl.NO_PROPAGATION),
            AttributeMetadata.create(AttributeTtl.NO_PROPAGATION))
        .addEqualityGroup(AttributeMetadata.create(AttributeTtl.UNLIMITED_PROPAGATION))
        .testEquals();
  }
}
