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
import io.opentelemetry.tags.EmptyTagMap;
import io.opentelemetry.tags.TagMap;

/**
 * Utility methods for accessing the {@link TagMap} contained in the {@link io.grpc.Context}.
 *
 * <p>Most code should interact with the current context via the public APIs in {@link TagMap} and
 * avoid accessing this class directly.
 *
 * @since 0.1.0
 */
public final class ContextUtils {
  private static final Context.Key<TagMap> TAG_MAP_KEY =
      Context.keyWithDefault("opentelemetry-tag-map-key", EmptyTagMap.getInstance());

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param tagMap the value to be set.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withValue(TagMap tagMap) {
    return Context.current().withValue(TAG_MAP_KEY, tagMap);
  }

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param tagMap the value to be set.
   * @param context the parent {@code Context}.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withValue(TagMap tagMap, Context context) {
    return context.withValue(TAG_MAP_KEY, tagMap);
  }

  /**
   * Returns the value from the current {@code Context}.
   *
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static TagMap getValue() {
    return TAG_MAP_KEY.get();
  }

  /**
   * Returns the value from the specified {@code Context}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.1.0
   */
  public static TagMap getValue(Context context) {
    return TAG_MAP_KEY.get(context);
  }

  /**
   * Returns a new {@link Scope} encapsulating the provided {@code TagMap} added to the current
   * {@code Context}.
   *
   * @param tagMap the {@code TagMap} to be added to the current {@code Context}.
   * @return the {@link Scope} for the updated {@code Context}.
   * @since 0.1.0
   */
  public static Scope withTagMap(TagMap tagMap) {
    return TagMapInScope.create(tagMap);
  }

  private ContextUtils() {}
}
