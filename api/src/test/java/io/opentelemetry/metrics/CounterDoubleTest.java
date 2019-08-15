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

import io.opentelemetry.OpenTelemetry;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CounterDouble}. */
@RunWith(JUnit4.class)
public class CounterDoubleTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<String> LABEL_KEY = Collections.singletonList("key");
  private static final List<String> EMPTY_LABEL_VALUES = Collections.emptyList();

  private final Meter meter = OpenTelemetry.getMeter();

  @Test
  public void noopGetOrCreateTimeSeries_WithNullLabelValues() {
    CounterDouble counterDouble =
        meter
            .counterDoubleBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setLabelKeys(LABEL_KEY)
            .setUnit(UNIT)
            .build();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    counterDouble.getOrCreateTimeSeries(null);
  }

  @Test
  public void noopGetOrCreateTimeSeries_WithInvalidLabelSize() {
    CounterDouble counterDouble =
        meter
            .counterDoubleBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setLabelKeys(LABEL_KEY)
            .setUnit(UNIT)
            .build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    counterDouble.getOrCreateTimeSeries(EMPTY_LABEL_VALUES);
  }

  @Test
  public void noopRemoveTimeSeries_WithNullLabelValues() {
    CounterDouble counterDouble =
        meter
            .counterDoubleBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setLabelKeys(LABEL_KEY)
            .setUnit(UNIT)
            .build();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    counterDouble.removeTimeSeries(null);
  }
}
