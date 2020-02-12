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

package io.opentelemetry.sdk.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AbstractInstrument}. */
@RunWith(JUnit4.class)
public class AbstractInstrumentTest {
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Map<String, String> CONSTANT_LABELS =
      Collections.singletonMap("key_2", "value_2");
  private static final List<String> LABEL_KEY = Collections.singletonList("key");
  private static final MeterSharedState METER_SHARED_STATE =
      MeterSharedState.create(TestClock.create(), Resource.getEmpty());

  @Test
  public void getValues() {
    TestInstrument testInstrument =
        new TestInstrument(NAME, DESCRIPTION, UNIT, CONSTANT_LABELS, LABEL_KEY, METER_SHARED_STATE);
    assertThat(testInstrument.getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(testInstrument.getUnit()).isEqualTo(UNIT);
    assertThat(testInstrument.getConstantLabels()).isEqualTo(CONSTANT_LABELS);
    assertThat(testInstrument.getLabelKeys()).isEqualTo(LABEL_KEY);
    assertThat(testInstrument.getMeterSharedState()).isEqualTo(METER_SHARED_STATE);
  }

  private static final class TestInstrument extends AbstractInstrument {
    TestInstrument(
        String name,
        String description,
        String unit,
        Map<String, String> constantLabels,
        List<String> labelKeys,
        MeterSharedState meterSharedState) {
      super(name, description, unit, constantLabels, labelKeys, meterSharedState);
    }
  }
}
