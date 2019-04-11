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

import io.opencensus.tags.TaggingState;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagPropagationComponent;

public final class TagsComponentShim extends TagsComponent {

  @Override
  public TaggerShim getTagger() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TagPropagationComponent getTagPropagationComponent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TaggingState getState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(TaggingState state) {
    throw new UnsupportedOperationException();
  }
}
