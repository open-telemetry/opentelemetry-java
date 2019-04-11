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

package openconsensus.opencensusshim.trace.export;

import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.export.RunningSpanStore;
import io.opencensus.trace.export.SampledSpanStore;
import io.opencensus.trace.export.SpanExporter;

public final class ExportComponentShim extends ExportComponent {

  @Override
  public SpanExporter getSpanExporter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RunningSpanStore getRunningSpanStore() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SampledSpanStore getSampledSpanStore() {
    throw new UnsupportedOperationException();
  }
}
