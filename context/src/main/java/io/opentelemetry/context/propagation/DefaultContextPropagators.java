/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code DefaultContextPropagators} is the default, built-in implementation of {@link
 * ContextPropagators}.
 *
 * <p>All the registered propagators are stored internally as a simple list, and are invoked
 * synchronically upon injection and extraction.
 *
 * <p>The propagation fields retrieved from all registered propagators are de-duplicated.
 *
 * @deprecated Use {@link ContextPropagators#create(TextMapPropagator)}
 */
@Deprecated
public final class DefaultContextPropagators implements ContextPropagators {

  private static final ContextPropagators NOOP =
      new DefaultContextPropagators(NoopTextMapPropagator.getInstance());

  static ContextPropagators noop() {
    return NOOP;
  }

  private final TextMapPropagator textMapPropagator;

  @Override
  public TextMapPropagator getTextMapPropagator() {
    return textMapPropagator;
  }

  /**
   * Returns a {@link DefaultContextPropagators.Builder} to create a new {@link ContextPropagators}
   * object.
   *
   * @return a {@link DefaultContextPropagators.Builder}.
   * @deprecated Use {@link ContextPropagators#create(TextMapPropagator)}
   */
  @Deprecated
  public static Builder builder() {
    return new Builder();
  }

  DefaultContextPropagators(TextMapPropagator textMapPropagator) {
    this.textMapPropagator = textMapPropagator;
  }

  /**
   * A builder of {@link DefaultContextPropagators}.
   *
   * @deprecated Use {@link ContextPropagators#create(TextMapPropagator)}
   */
  @Deprecated
  public static final class Builder {
    List<TextMapPropagator> textPropagators = new ArrayList<>();

    /**
     * Add a {@link TextMapPropagator}.
     *
     * @deprecated Use {@link ContextPropagators#create(TextMapPropagator)}
     */
    @Deprecated
    public Builder addTextMapPropagator(TextMapPropagator textMapPropagator) {
      if (textMapPropagator == null) {
        throw new NullPointerException("textMapPropagator");
      }

      textPropagators.add(textMapPropagator);
      return this;
    }

    /**
     * Returns a {@link ContextPropagators}.
     *
     * @deprecated Use {@link ContextPropagators#create(TextMapPropagator)}
     */
    @Deprecated
    public ContextPropagators build() {
      return ContextPropagators.create(TextMapPropagator.composite(textPropagators));
    }
  }
}
