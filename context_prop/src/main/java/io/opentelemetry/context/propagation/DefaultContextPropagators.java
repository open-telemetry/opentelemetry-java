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

package io.opentelemetry.context.propagation;

import io.grpc.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code DefaultContextPropagators} is the default, built-in implementation of {@link
 * ContextPropagators}.
 *
 * <p>All the registered propagators are stored internally as a simple list, and are invoked
 * synchronically upon injection and extraction.
 *
 * @since 0.3.0
 */
public final class DefaultContextPropagators implements ContextPropagators {
  private final HttpTextFormat textFormat;

  @Override
  public HttpTextFormat getHttpTextFormat() {
    return textFormat;
  }

  /**
   * Returns a {@link DefaultContextPropagators.Builder} to create a new {@link ContextPropagators}
   * object.
   *
   * @return a {@link DefaultContextPropagators.Builder}.
   * @since 0.3.0
   */
  public static Builder builder() {
    return new Builder();
  }

  private DefaultContextPropagators(HttpTextFormat textFormat) {
    this.textFormat = textFormat;
  }

  /**
   * {@link Builder} is used to construct a new {@code ContextPropagators} object with the specified
   * propagators.
   *
   * <p>This is a example of a {@code ContextPropagators} object being created:
   *
   * <pre>{@code
   * ContextPropagators propagators = DefaultContextPropagators.builder()
   *     .addHttpTextFormat(new HttpTraceContext())
   *     .addHttpTextFormat(new HttpCorrelationContext())
   *     .addHttpTextFormat(new MyCustomContextPropagator())
   *     .build();
   * }</pre>
   *
   * @since 0.3.0
   */
  public static final class Builder {
    List<HttpTextFormat> textPropagators = new ArrayList<>();

    /**
     * Adds a {@link HttpTextFormat} propagator.
     *
     * <p>One propagator per concern (traces, correlations, etc) should be added if this format is
     * supported.
     *
     * @param textFormat the propagator to be added.
     * @return this.
     * @throws NullPointerException if {@code textFormat} is {@code null}.
     * @since 0.3.0
     */
    public Builder addHttpTextFormat(HttpTextFormat textFormat) {
      if (textFormat == null) {
        throw new NullPointerException("textFormat");
      }

      textPropagators.add(textFormat);
      return this;
    }

    /**
     * Builds a new {@code ContextPropagators} with the specified propagators.
     *
     * @return the newly created {@code ContextPropagators} instance.
     * @since 0.3.0
     */
    public ContextPropagators build() {
      if (textPropagators.isEmpty()) {
        return new DefaultContextPropagators(NoopHttpTextFormat.INSTANCE);
      }

      return new DefaultContextPropagators(new MultiHttpTextFormat(textPropagators));
    }
  }

  private static final class MultiHttpTextFormat implements HttpTextFormat {
    private final HttpTextFormat[] textPropagators;
    private final List<String> allFields;

    private MultiHttpTextFormat(List<HttpTextFormat> textPropagators) {
      this.textPropagators = new HttpTextFormat[textPropagators.size()];
      textPropagators.toArray(this.textPropagators);
      this.allFields = getAllFields(this.textPropagators);
    }

    @Override
    public List<String> fields() {
      return allFields;
    }

    private static List<String> getAllFields(HttpTextFormat[] textPropagators) {
      List<String> fields = new ArrayList<>();
      for (int i = 0; i < textPropagators.length; i++) {
        fields.addAll(textPropagators[i].fields());
      }

      return fields;
    }

    @Override
    public <C> void inject(Context context, C carrier, Setter<C> setter) {
      for (int i = 0; i < textPropagators.length; i++) {
        textPropagators[i].inject(context, carrier, setter);
      }
    }

    @Override
    public <C> Context extract(Context context, C carrier, Getter<C> getter) {
      for (int i = 0; i < textPropagators.length; i++) {
        context = textPropagators[i].extract(context, carrier, getter);
      }
      return context;
    }
  }

  private static final class NoopHttpTextFormat implements HttpTextFormat {
    private static final NoopHttpTextFormat INSTANCE = new NoopHttpTextFormat();

    @Override
    public List<String> fields() {
      return Collections.emptyList();
    }

    @Override
    public <C> void inject(Context context, C carrier, Setter<C> setter) {}

    @Override
    public <C> Context extract(Context context, C carrier, Getter<C> getter) {
      return context;
    }
  }
}
