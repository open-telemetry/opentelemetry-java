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
import io.opentelemetry.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code DefaultPropagators} is the default, built-in implementation of {@link Propagators}.
 *
 * <p>All the registered propagators are stored internally as a simple list, and are invoked
 * synchronically upon injection and extraction.
 *
 * @since 0.3.0
 */
public final class DefaultPropagators implements Propagators {
  private final HttpTextFormat textFormat;

  @Override
  public HttpTextFormat getHttpTextFormat() {
    return textFormat;
  }

  /**
   * Returns a {@link Propagators.Builder} to create a new {@link Propagators} object.
   *
   * <p>See {@link Propagators.Builder}.
   *
   * @return a {@link Propagators.Builder}.
   * @since 0.3.0
   */
  public static Builder builder() {
    return new DefaultBuilder();
  }

  private DefaultPropagators(HttpTextFormat textFormat) {
    this.textFormat = textFormat;
  }

  private static final class DefaultBuilder implements Builder {
    List<HttpTextFormat> textPropagators = new ArrayList<>();

    @Override
    public Builder addHttpTextFormat(HttpTextFormat textFormat) {
      Utils.checkNotNull(textFormat, "textFormat");
      textPropagators.add(textFormat);
      return this;
    }

    @Override
    public Propagators build() {
      if (textPropagators.isEmpty()) {
        return new DefaultPropagators(NoopHttpTextFormat.INSTANCE);
      }

      return new DefaultPropagators(new MultiHttpTextFormat(textPropagators));
    }
  }

  private static final class MultiHttpTextFormat implements HttpTextFormat {
    private final HttpTextFormat[] textPropagators;

    private MultiHttpTextFormat(List<HttpTextFormat> textPropagators) {
      this.textPropagators = new HttpTextFormat[textPropagators.size()];
      textPropagators.toArray(this.textPropagators);
    }

    @Override
    public List<String> fields() {
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
      return Collections.<String>emptyList();
    }

    @Override
    public <C> void inject(Context context, C carrier, Setter<C> setter) {}

    @Override
    public <C> Context extract(Context context, C carrier, Getter<C> getter) {
      return context;
    }
  }
}
