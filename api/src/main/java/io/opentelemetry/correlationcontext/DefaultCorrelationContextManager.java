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

package io.opentelemetry.correlationcontext;

import io.opentelemetry.context.Scope;
import io.opentelemetry.internal.Utils;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link CorrelationContextManager}.
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class DefaultCorrelationContextManager implements CorrelationContextManager {
  private static final DefaultCorrelationContextManager INSTANCE =
      new DefaultCorrelationContextManager();

  /**
   * Returns a {@code CorrelationContextManager} singleton that is the default implementation for
   * {@link CorrelationContextManager}.
   *
   * @return a {@code CorrelationContextManager} singleton that is the default implementation for
   *     {@link CorrelationContextManager}.
   */
  public static CorrelationContextManager getInstance() {
    return INSTANCE;
  }

  @Override
  public CorrelationContext getCurrentContext() {
    return CorrelationsContextUtils.getCurrentCorrelationContext();
  }

  @Override
  public CorrelationContext.Builder contextBuilder() {
    return new NoopCorrelationContextBuilder();
  }

  @Override
  public Scope withContext(CorrelationContext distContext) {
    return CorrelationsContextUtils.currentContextWith(distContext);
  }

  @Immutable
  private static final class NoopCorrelationContextBuilder implements CorrelationContext.Builder {
    @Override
    public CorrelationContext.Builder setParent(CorrelationContext parent) {
      Utils.checkNotNull(parent, "parent");
      return this;
    }

    @Override
    public CorrelationContext.Builder setNoParent() {
      return this;
    }

    @Override
    public CorrelationContext.Builder put(
        EntryKey key, EntryValue value, EntryMetadata entryMetadata) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      Utils.checkNotNull(entryMetadata, "entryMetadata");
      return this;
    }

    @Override
    public CorrelationContext.Builder remove(EntryKey key) {
      Utils.checkNotNull(key, "key");
      return this;
    }

    @Override
    public CorrelationContext build() {
      return EmptyCorrelationContext.getInstance();
    }
  }
}
