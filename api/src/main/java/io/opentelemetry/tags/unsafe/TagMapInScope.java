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

package io.opentelemetry.tags.unsafe;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.tags.TagMap;

/**
 * A scope that manages the {@link Context} for a {@link TagMap}.
 *
 * @since 0.1.0
 */
final class TagMapInScope implements Scope {
  private final Context orig;

  private TagMapInScope(TagMap tags) {
    orig = ContextUtils.withValue(tags).attach();
  }

  /**
   * Constructs a new {@link TagMapInScope}.
   *
   * @param tags the {@code TagMap} to be added to the current {@code Context}.
   * @since 0.1.0
   */
  static TagMapInScope create(TagMap tags) {
    return new TagMapInScope(tags);
  }

  @Override
  public void close() {
    Context.current().detach(orig);
  }
}
