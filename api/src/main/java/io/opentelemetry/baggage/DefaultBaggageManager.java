/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Objects;
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
  public Scope withBaggage(Baggage baggage) {
    return BaggageUtils.currentContextWith(baggage);
  }

  @Immutable
  private static final class NoopBaggageBuilder implements Baggage.Builder {
    @Override
    public Baggage.Builder setParent(Context context) {
      Objects.requireNonNull(context, "context");
      return this;
    }

    @Override
    public Baggage.Builder setNoParent() {
      return this;
    }

    @Override
    public Baggage.Builder put(String key, String value, EntryMetadata entryMetadata) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      Objects.requireNonNull(entryMetadata, "entryMetadata");
      return this;
    }

    @Override
    public Baggage.Builder remove(String key) {
      Objects.requireNonNull(key, "key");
      return this;
    }

    @Override
    public Baggage build() {
      return EmptyBaggage.getInstance();
    }
  }
}
