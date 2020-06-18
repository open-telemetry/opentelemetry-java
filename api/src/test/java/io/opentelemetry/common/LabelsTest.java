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

import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Unit tests for {@link Labels}s. */
public class LabelsTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void forEach() {
    final Map<String, String> entriesSeen = new HashMap<>();

    Labels labels =
        Labels.of(
            "key1", "value1",
            "key2", "value2");

    labels.forEach((key, value) -> entriesSeen.put(key, value));

    assertThat(entriesSeen).containsExactly("key1", "value1", "key2", "value2");
  }

  @Test
  public void forEach_singleAttribute() {
    final Map<String, String> entriesSeen = new HashMap<>();

    Labels labels = Labels.of("key", "value");
    labels.forEach((key, value) -> entriesSeen.put(key, value));

    assertThat(entriesSeen).containsExactly("key", "value");
  }

  @Test
  public void forEach_empty() {
    final AtomicBoolean sawSomething = new AtomicBoolean(false);
    Labels emptyLabels = Labels.empty();
    emptyLabels.forEach((key, value) -> sawSomething.set(true));
    assertThat(sawSomething.get()).isFalse();
  }

  @Test
  public void orderIndependentEquality() {
    Labels one =
        Labels.of(
            "key3", "value3",
            "key1", "value1",
            "key2", "value2");
    Labels two =
        Labels.of(
            "key2", "value2",
            "key3", "value3",
            "key1", "value1");

    assertThat(one).isEqualTo(two);
  }

  @Test
  public void deduplication() {
    Labels one =
        Labels.of(
            "key1", "value1",
            "key1", "valueX");
    Labels two = Labels.of("key1", "value1");

    assertThat(one).isEqualTo(two);
  }

  @Test
  public void threeLabels() {
    Labels one =
        Labels.of(
            "key1", "value1",
            "key3", "value3",
            "key2", "value2");
    assertThat(one).isNotNull();
  }

  @Test
  public void fourLabels() {
    Labels one =
        Labels.of(
            "key1", "value1",
            "key2", "value2",
            "key3", "value3",
            "key4", "value4");
    assertThat(one).isNotNull();
  }

  @Test
  public void builder() {
    Labels labels =
        Labels.newBuilder()
            .setLabel("key1", "value1")
            .setLabel("key2", "value2")
            .setLabel("key1", "duplicateShouldBeIgnored")
            .build();

    assertThat(labels)
        .isEqualTo(
            Labels.of(
                "key1", "value1",
                "key2", "value2"));
  }
}
