/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * {@code DefaultContextPropagators} is the default, built-in implementation of {@link
 * ContextPropagators}.
 *
 * <p>All the registered propagators are stored internally as a simple list, and are invoked
 * synchronically upon injection and extraction.
 *
 * <p>The propagation fields retrieved from all registered propagators are de-duplicated.
 */
public final class DefaultContextPropagators implements ContextPropagators {
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
   */
  public static Builder builder() {
    return new Builder();
  }

  private DefaultContextPropagators(TextMapPropagator textMapPropagator) {
    this.textMapPropagator = textMapPropagator;
  }

  /**
   * {@link Builder} is used to construct a new {@code ContextPropagators} object with the specified
   * propagators.
   *
   * <p>Invocation order of {@code TextMapPropagator#inject()} and {@code
   * TextMapPropagator#extract()} for registered trace propagators is undefined.
   *
   * <p>This is a example of a {@code ContextPropagators} object being created:
   *
   * <pre>{@code
   * ContextPropagators propagators = DefaultContextPropagators.builder()
   *     .addTextMapPropagator(new HttpTraceContext())
   *     .addTextMapPropagator(new HttpBaggage())
   *     .addTextMapPropagator(new MyCustomContextPropagator())
   *     .build();
   * }</pre>
   */
  public static final class Builder {
    List<TextMapPropagator> textPropagators = new ArrayList<>();

    /**
     * Adds a {@link TextMapPropagator} propagator.
     *
     * <p>One propagator per concern (traces, correlations, etc) should be added if this format is
     * supported.
     *
     * @param textMapPropagator the propagator to be added.
     * @return this.
     * @throws NullPointerException if {@code textMapPropagator} is {@code null}.
     */
    public Builder addTextMapPropagator(TextMapPropagator textMapPropagator) {
      if (textMapPropagator == null) {
        throw new NullPointerException("textMapPropagator");
      }

      textPropagators.add(textMapPropagator);
      return this;
    }

    /**
     * Builds a new {@code ContextPropagators} with the specified propagators.
     *
     * @return the newly created {@code ContextPropagators} instance.
     */
    public ContextPropagators build() {
      if (textPropagators.isEmpty()) {
        return new DefaultContextPropagators(NoopTextMapPropagator.INSTANCE);
      }

      return new DefaultContextPropagators(new MultiTextMapPropagator(textPropagators));
    }
  }

  private static final class MultiTextMapPropagator implements TextMapPropagator {
    private final TextMapPropagator[] textPropagators;
    private final List<String> allFields;

    private MultiTextMapPropagator(List<TextMapPropagator> textPropagators) {
      this.textPropagators = new TextMapPropagator[textPropagators.size()];
      textPropagators.toArray(this.textPropagators);
      this.allFields = Collections.unmodifiableList(getAllFields(this.textPropagators));
    }

    @Override
    public List<String> fields() {
      return allFields;
    }

    private static List<String> getAllFields(TextMapPropagator[] textPropagators) {
      Set<String> fields = new LinkedHashSet<>();
      for (int i = 0; i < textPropagators.length; i++) {
        fields.addAll(textPropagators[i].fields());
      }

      return new ArrayList<>(fields);
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, Setter<C> setter) {
      for (int i = 0; i < textPropagators.length; i++) {
        textPropagators[i].inject(context, carrier, setter);
      }
    }

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter) {
      for (int i = 0; i < textPropagators.length; i++) {
        context = textPropagators[i].extract(context, carrier, getter);
      }
      return context;
    }
  }

  private static final class NoopTextMapPropagator implements TextMapPropagator {
    private static final NoopTextMapPropagator INSTANCE = new NoopTextMapPropagator();

    @Override
    public List<String> fields() {
      return Collections.emptyList();
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, Setter<C> setter) {}

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter) {
      return context;
    }
  }
}
