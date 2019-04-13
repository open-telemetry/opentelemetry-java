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

package openconsensus.trace.propagation;

import java.util.List;
import openconsensus.trace.SpanContext;

/**
 * An abstract class that implements {@code TextFormat}.
 *
 * <p>Users are encouraged to extend this class for convenience.
 *
 * @since 0.1.0
 */
public abstract class AbstractTextFormat implements TextFormat {
  @Override
  public abstract List<String> fields();

  @Override
  public abstract <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter);

  @Override
  public abstract <C> SpanContext extract(C carrier, Getter<C> getter)
      throws SpanContextParseException;

  protected AbstractTextFormat() {}
}
