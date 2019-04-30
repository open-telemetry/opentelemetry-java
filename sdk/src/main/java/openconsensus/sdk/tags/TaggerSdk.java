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

package openconsensus.sdk.tags;

import openconsensus.context.Scope;
import openconsensus.context.propagation.BinaryFormat;
import openconsensus.context.propagation.HttpTextFormat;
import openconsensus.sdk.tags.internal.CurrentTagMapUtils;
import openconsensus.tags.TagMap;
import openconsensus.tags.TagMapBuilder;
import openconsensus.tags.Tagger;

public class TaggerSdk implements Tagger {

  @Override
  public TagMap getCurrentTagMap() {
    return CurrentTagMapUtils.getCurrentTagMap();
  }

  @Override
  public TagMapBuilder emptyBuilder() {
    return null;
  }

  @Override
  public TagMapBuilder toBuilder(TagMap tags) {
    return null;
  }

  @Override
  public TagMapBuilder currentBuilder() {
    return null;
  }

  @Override
  public Scope withTagMap(TagMap tags) {
    return CurrentTagMapUtils.withTagMap(tags);
  }

  @Override
  public BinaryFormat<TagMap> getBinaryFormat() {
    return null;
  }

  @Override
  public HttpTextFormat<TagMap> getHttpTextFormat() {
    return null;
  }
}
