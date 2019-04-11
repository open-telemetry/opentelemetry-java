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

package openconsensus.opencensusshim.tags;

import io.opencensus.common.Scope;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.Tagger;

public final class TaggerShim extends Tagger {

  @Override
  public TagContext empty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TagContext getCurrentTagContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TagContextBuilder emptyBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TagContextBuilder toBuilder(TagContext tags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TagContextBuilder currentBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Scope withTagContext(TagContext tags) {
    throw new UnsupportedOperationException();
  }
}
