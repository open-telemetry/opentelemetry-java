/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImmutableKeyValuePairsTest {

  @Test
  void toStringIsCorrect() {
    assertThat(new TestPairs(Collections.emptyList()).toString()).isEqualTo("{}");
    assertThat(new TestPairs(Arrays.asList("one", 55)).toString()).isEqualTo("{one=55}");
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).toString())
        .isEqualTo("{one=55, two=b}");
    assertThat(new TestPairs(Arrays.asList("one", 55, "two", "b")).toString())
        .isEqualTo(ImmutableMap.of("one", 55, "two", "b").toString());
  }

  static class TestPairs extends ImmutableKeyValuePairs<Object> {
    private final List<Object> data;

    TestPairs(List<Object> data) {
      this.data = data;
    }

    @Override
    List<Object> data() {
      return data;
    }
  }
}
