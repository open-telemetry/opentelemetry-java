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

import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.MetricsComponent;
import io.opencensus.metrics.export.ExportComponent;

public final class MetricsComponentShim extends MetricsComponent {

  @Override
  public ExportComponent getExportComponent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetricRegistry getMetricRegistry() {
    throw new UnsupportedOperationException();
  }
}
