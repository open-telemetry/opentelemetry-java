/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.metrics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricRegistry}. */
@RunWith(JUnit4.class)
public class MetricRegistryTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "test_name";
  private static final String NAME_2 = "test_name2";
  private static final String DESCRIPTION = "test_description";
  private static final String UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("test_key", "test key description");
  private static final List<LabelKey> LABEL_KEYS = Collections.singletonList(LABEL_KEY);
  private static final LabelValue LABEL_VALUE = LabelValue.create("test_value");
  private static final LabelValue LABEL_VALUE_2 = LabelValue.create("test_value_2");
  private static final List<LabelValue> LABEL_VALUES = Collections.singletonList(LABEL_VALUE);
  private static final Map<LabelKey, LabelValue> CONSTANT_LABELS =
      Collections.singletonMap(
          LabelKey.create("test_key_1", "test key description"), LABEL_VALUE_2);
  private static final MetricOptions METRIC_OPTIONS =
      MetricOptions.builder()
          .setDescription(DESCRIPTION)
          .setUnit(UNIT)
          .setLabelKeys(LABEL_KEYS)
          .setConstantLabels(CONSTANT_LABELS)
          .build();
  private final MetricRegistry metricRegistry = NoopMetrics.newNoopMetricRegistry();

  @Test
  public void noopAddLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addLongGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDoubleGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDerivedLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedLongGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDerivedDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedDoubleGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDoubleCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDoubleCumulative(null, METRIC_OPTIONS);
  }

  @Test
  public void noopAddDerivedDoubleCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedDoubleCumulative(null, METRIC_OPTIONS);
  }
}
