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

package io.opentelemetry.opencensusshim.tags.unsafe;

import io.grpc.Context;
import io.opentelemetry.opencensusshim.tags.Tag;
import io.opentelemetry.opencensusshim.tags.TagContext;
import java.util.Collections;
import java.util.Iterator;
import javax.annotation.concurrent.Immutable;

/**
 * Utility methods for accessing the {@link TagContext} contained in the {@link Context}.
 *
 * <p>Most code should interact with the current context via the public APIs in {@link TagContext}
 * and avoid accessing {@link #TAG_CONTEXT_KEY} directly.
 *
 * @since 0.1.0
 */
public final class ContextUtils {
  private static final TagContext EMPTY_TAG_CONTEXT = new EmptyTagContext();

  private ContextUtils() {}

  /**
   * The {@link Context.Key} used to interact with the {@code TagContext} contained in the {@link
   * Context}.
   *
   * @since 0.1.0
   */
  public static final Context.Key<TagContext> TAG_CONTEXT_KEY =
      Context.keyWithDefault("opencensus-tag-context-key", EMPTY_TAG_CONTEXT);

  @Immutable
  private static final class EmptyTagContext extends TagContext {

    @Override
    protected Iterator<Tag> getIterator() {
      return Collections.<Tag>emptySet().iterator();
    }
  }
}
