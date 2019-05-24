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

package io.opentelemetry.sdk.tags.internal;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.unsafe.ContextUtils;
import io.opentelemetry.tags.unsafe.TagMapInScope;

public final class CurrentTagMapUtils {
  private CurrentTagMapUtils() {}

  /**
   * Returns the {@link TagMap} from the current context.
   *
   * @return the {@code TagMap} from the current context.
   */
  public static TagMap getCurrentTagMap() {
    return ContextUtils.getValue(Context.current());
  }

  /**
   * Enters the scope of code where the given {@link TagMap} is in the current context and returns
   * an object that represents that scope. The scope is exited when the returned object is closed.
   *
   * @param tags the {@code TagMap} to be set to the current context.
   * @return an object that defines a scope where the given {@code TagMap} is set to the current
   *     context.
   */
  public static Scope withTagMap(TagMap tags) {
    return TagMapInScope.create(tags);
  }
}
