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

package openconsensus.opencensusshim.trace.propagation;

import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.PropagationComponent;
import io.opencensus.trace.propagation.TextFormat;

public final class PropagationComponentShim extends PropagationComponent {

  @Override
  public BinaryFormat getBinaryFormat() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TextFormat getB3Format() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TextFormat getTraceContextFormat() {
    throw new UnsupportedOperationException();
  }
}
