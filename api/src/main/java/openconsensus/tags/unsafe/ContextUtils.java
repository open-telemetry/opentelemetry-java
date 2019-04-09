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
import javax.annotation.concurrent.Immutable;
import openconsensus.tags.TagMap;

/**
 * Utility methods for accessing the {@link TagMap} contained in the {@link io.grpc.Context}.
 *
 * <p>Most code should interact with the current context via the public APIs in {@link TagMap} and
 * avoid accessing {@link #TAG_CONTEXT_KEY} directly.
 *
 * @since 0.1.0
 */
public final class ContextUtils {
  private static final TagMap EMPTY_TAG_CONTEXT = new EmptyTagMap();

  private ContextUtils() {}

  /**
   * The {@link io.grpc.Context.Key} used to interact with the {@code TagMap} contained in the
   * {@link io.grpc.Context}.
   *
   * @since 0.1.0
   */
  public static final Context.Key<TagMap> TAG_CONTEXT_KEY =
      Context.keyWithDefault("openconsensus-tag-context-key", EMPTY_TAG_CONTEXT);

  @Immutable
  private static final class EmptyTagMap extends TagMap {}
}
