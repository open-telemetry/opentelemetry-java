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

package openconsensus.opencensusshim.trace;

import io.opencensus.trace.TraceComponent;
import openconsensus.opencensusshim.common.ClockShim;
import openconsensus.opencensusshim.trace.config.TraceConfigShim;
import openconsensus.opencensusshim.trace.export.ExportComponentShim;
import openconsensus.opencensusshim.trace.propagation.PropagationComponentShim;

public final class TraceComponentShim extends TraceComponent {

  @Override
  public TracerShim getTracer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PropagationComponentShim getPropagationComponent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ClockShim getClock() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExportComponentShim getExportComponent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TraceConfigShim getTraceConfig() {
    throw new UnsupportedOperationException();
  }
}
