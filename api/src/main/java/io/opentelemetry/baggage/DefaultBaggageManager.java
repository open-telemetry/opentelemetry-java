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

package io.opentelemetry.baggage;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.internal.Utils;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link BaggageManager}.
 *
 * @since 0.9.0
 */
@ThreadSafe
public final class DefaultBaggageManager implements BaggageManager {
  private static final DefaultBaggageManager INSTANCE = new DefaultBaggageManager();

  /**
   * Returns a {@code BaggageManager} singleton that is the default implementation for {@link
   * BaggageManager}.
   *
   * @return a {@code BaggageManager} singleton that is the default implementation for {@link
   *     BaggageManager}.
   */
  public static BaggageManager getInstance() {
    return INSTANCE;
  }

  @Override
  public Baggage getCurrentBaggage() {
    return BaggageUtils.getCurrentBaggage();
  }

  @Override
  public Baggage.Builder baggageBuilder() {
    return new NoopBaggageBuilder();
  }

  @Override
  public Scope withContext(Baggage distContext) {
    return BaggageUtils.currentContextWith(distContext);
  }

  @Immutable
  private static final class NoopBaggageBuilder implements Baggage.Builder {
    @Override
    public Baggage.Builder setParent(Baggage parent) {
      Utils.checkNotNull(parent, "parent");
      return this;
    }

    @Override
    public Baggage.Builder setParent(Context context) {
      Utils.checkNotNull(context, "context");
      return this;
    }

    @Override
    public Baggage.Builder setNoParent() {
      return this;
    }

    @Override
    public Baggage.Builder put(String key, String value, EntryMetadata entryMetadata) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      Utils.checkNotNull(entryMetadata, "entryMetadata");
      return this;
    }

    @Override
    public Baggage.Builder remove(String key) {
      Utils.checkNotNull(key, "key");
      return this;
    }

    @Override
    public Baggage build() {
      return EmptyBaggage.getInstance();
    }
  }
}
