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

import io.opencensus.metrics.DerivedDoubleGauge;
import io.opencensus.metrics.DerivedLongGauge;
import io.opencensus.metrics.DoubleGauge;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.MetricOptions;
import io.opencensus.metrics.MetricRegistry;

public final class MetricRegistryShim extends MetricRegistry {

  @Override
  public LongGauge addLongGauge(String name, MetricOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DoubleGauge addDoubleGauge(String name, MetricOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DerivedLongGauge addDerivedLongGauge(String name, MetricOptions options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DerivedDoubleGauge addDerivedDoubleGauge(String name, MetricOptions options) {
    throw new UnsupportedOperationException();
  }
}
