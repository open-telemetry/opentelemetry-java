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

package openconsensus.opencensusshim.metrics;

import io.opencensus.common.ToLongFunction;
import io.opencensus.metrics.DerivedLongGauge;
import io.opencensus.metrics.LabelValue;
import java.util.List;

public final class DerivedLongGaugeShim extends DerivedLongGauge {

  @Override
  public <T> void createTimeSeries(List<LabelValue> labelValues, T obj,
      ToLongFunction<T> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeTimeSeries(List<LabelValue> labelValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}
