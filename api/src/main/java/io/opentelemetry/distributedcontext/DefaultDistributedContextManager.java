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

package io.opentelemetry.distributedcontext;

import io.opentelemetry.context.Scope;
import io.opentelemetry.distributedcontext.unsafe.ContextUtils;
import io.opentelemetry.internal.Utils;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link DistributedContextManager}.
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class DefaultDistributedContextManager implements DistributedContextManager {
  private static final DefaultDistributedContextManager INSTANCE =
      new DefaultDistributedContextManager();

  /**
   * Returns a {@code DistributedContextManager} singleton that is the default implementation for
   * {@link DistributedContextManager}.
   *
   * @return a {@code DistributedContextManager} singleton that is the default implementation for
   *     {@link DistributedContextManager}.
   * @since 0.1.0
   */
  public static DistributedContextManager getInstance() {
    return INSTANCE;
  }

  @Override
  public DistributedContext getCurrentContext() {
    return ContextUtils.getValue();
  }

  @Override
  public DistributedContext.Builder contextBuilder() {
    return new NoopDistributedContextBuilder();
  }

  @Override
  public Scope withContext(DistributedContext distContext) {
    return ContextUtils.withDistributedContext(distContext);
  }

  @Immutable
  private static final class NoopDistributedContextBuilder implements DistributedContext.Builder {
    @Override
    public DistributedContext.Builder setParent(DistributedContext parent) {
      Utils.checkNotNull(parent, "parent");
      return this;
    }

    @Override
    public DistributedContext.Builder setNoParent() {
      return this;
    }

    @Override
    public DistributedContext.Builder put(
        EntryKey key, EntryValue value, EntryMetadata tagMetadata) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      Utils.checkNotNull(tagMetadata, "tagMetadata");
      return this;
    }

    @Override
    public DistributedContext.Builder remove(EntryKey key) {
      Utils.checkNotNull(key, "key");
      return this;
    }

    @Override
    public DistributedContext build() {
      return EmptyDistributedContext.getInstance();
    }
  }
}
