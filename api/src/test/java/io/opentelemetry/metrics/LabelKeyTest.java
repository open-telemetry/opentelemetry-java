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

package io.opentelemetry.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LabelKey}. */
@RunWith(JUnit4.class)
public class LabelKeyTest {

  private static final LabelKey KEY = LabelKey.create("key", "description");

  @Test
  public void testGetKey() {
    assertThat(KEY.getKey()).isEqualTo("key");
  }

  @Test
  public void testGetDescription() {
    assertThat(KEY.getDescription()).isEqualTo("description");
  }

  @Test
  public void create_NoLengthConstraint() {
    // We have a length constraint of 256-characters for AttributeKey. That constraint doesn't apply
    // to
    // LabelKey.
    char[] chars = new char[300];
    Arrays.fill(chars, 'k');
    String key = new String(chars);
    assertThat(LabelKey.create(key, "").getKey()).isEqualTo(key);
  }

  @Test
  public void create_WithUnprintableChars() {
    String key = "\2ab\3cd";
    String description = "\4ef\5gh";
    LabelKey labelKey = LabelKey.create(key, description);
    assertThat(labelKey.getKey()).isEqualTo(key);
    assertThat(labelKey.getDescription()).isEqualTo(description);
  }

  @Test
  public void create_WithNonAsciiChars() {
    String key = "键";
    String description = "测试用键";
    LabelKey nonAsciiKey = LabelKey.create(key, description);
    assertThat(nonAsciiKey.getKey()).isEqualTo(key);
    assertThat(nonAsciiKey.getDescription()).isEqualTo(description);
  }

  @Test
  public void create_Empty() {
    LabelKey emptyKey = LabelKey.create("", "");
    assertThat(emptyKey.getKey()).isEmpty();
    assertThat(emptyKey.getDescription()).isEmpty();
  }

  @Test
  public void testLabelKeyEquals() {
    new EqualsTester()
        .addEqualityGroup(LabelKey.create("foo", ""), LabelKey.create("foo", ""))
        .addEqualityGroup(LabelKey.create("foo", "description"))
        .addEqualityGroup(LabelKey.create("bar", ""))
        .testEquals();
  }
}
