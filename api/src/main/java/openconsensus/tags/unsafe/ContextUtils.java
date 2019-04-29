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

package openconsensus.tags.unsafe;

import io.grpc.Context;
import openconsensus.internal.Utils;
import openconsensus.tags.EmptyTagMap;
import openconsensus.tags.TagMap;

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
      Context.keyWithDefault("openconsensus-tag-map-key", EmptyTagMap.INSTANCE);

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param context the parent {@code Context}.
   * @param tagMap the value to be set.
   * @return a new context with the given value set.
   * @since 0.1.0
   */
  public static Context withValue(Context context, TagMap tagMap) {
    return Utils.checkNotNull(context, "context").withValue(TAG_MAP_KEY, tagMap);
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

  private ContextUtils() {}
}
