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

/** Unit tests for {@link GaugeDouble}. */
@RunWith(JUnit4.class)
public class GaugeDoubleTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<String> LABEL_KEY = Collections.singletonList("key");
  private static final List<String> EMPTY_LABEL_VALUES = Collections.emptyList();

  private final Meter meter = OpenTelemetry.getMeter();

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.gaugeDoubleBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.gaugeDoubleBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.gaugeDoubleBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void noopGetHandle_WithNullLabelValues() {
    GaugeDouble gaugeDouble =
        meter
            .gaugeDoubleBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setLabelKeys(LABEL_KEY)
            .setUnit(UNIT)
            .build();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    gaugeDouble.getHandle(null);
  }

  @Test
  public void noopGetHandle_WithInvalidLabelSize() {
    GaugeDouble gaugeDouble =
        meter
            .gaugeDoubleBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setLabelKeys(LABEL_KEY)
            .setUnit(UNIT)
            .build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    gaugeDouble.getHandle(EMPTY_LABEL_VALUES);
  }

  @Test
  public void noopRemoveHandle_WithNullLabelValues() {
    GaugeDouble gaugeDouble =
        meter
            .gaugeDoubleBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setLabelKeys(LABEL_KEY)
            .setUnit(UNIT)
            .build();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    gaugeDouble.removeHandle(null);
  }
}
