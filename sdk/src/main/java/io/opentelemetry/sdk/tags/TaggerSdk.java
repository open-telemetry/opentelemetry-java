/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.tags;

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.Tagger;
import io.opentelemetry.tags.unsafe.ContextUtils;
import io.opentelemetry.tags.unsafe.TagMapInScope;

/** {@link TaggerSdk} is SDK implementation of {@link Tagger}. */
public class TaggerSdk implements Tagger {

  @Override
  public TagMap getCurrentTagMap() {
    return ContextUtils.getValue();
  }

  @Override
  public TagMap.Builder emptyBuilder() {
    return null;
  }

  @Override
  public TagMap.Builder toBuilder(TagMap tags) {
    return null;
  }

  @Override
  public TagMap.Builder currentBuilder() {
    return null;
  }

  @Override
  public Scope withTagMap(TagMap tags) {
    return TagMapInScope.create(tags);
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
