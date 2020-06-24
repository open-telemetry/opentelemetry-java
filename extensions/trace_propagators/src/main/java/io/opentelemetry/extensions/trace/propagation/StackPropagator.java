/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.extensions.trace.propagation;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/** Implementation of a composite propagator for trace. TODO: Include Text + Trace in the name. */
@Immutable
public class StackPropagator implements HttpTextFormat {
  private final HttpTextFormat[] propagators;
  private final List<String> propagatorsFields;

  private StackPropagator(List<HttpTextFormat> propagatorList) {
    this.propagators = new HttpTextFormat[propagatorList.size()];
    propagatorList.toArray(this.propagators);

    List<String> fields = new ArrayList<>();
    for (HttpTextFormat propagator : propagators) {
      fields.addAll(propagator.fields());
    }
    this.propagatorsFields = Collections.unmodifiableList(fields);
  }

  /** Hello. */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public List<String> fields() {
    return propagatorsFields;
  }

  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    for (int i = 0; i < propagators.length; i++) {
      propagators[i].inject(context, carrier, setter);
    }
  }

  @Override
  public <C> Context extract(Context context, C carrier, Getter<C> getter) {
    for (int i = 0; i < propagators.length; i++) {
      context = propagators[i].extract(context, carrier, getter);
      if (TracingContextUtils.getSpanWithoutDefault(context) != null) {
        break;
      }
    }

    return context;
  }

  /** Hello. */
  public static class Builder {
    private final List<HttpTextFormat> propagators;

    private Builder() {
      propagators = new ArrayList<>();
    }

    /** Hello. */
    public Builder addPropagator(HttpTextFormat textPropagator) {
      Objects.requireNonNull(textPropagator, "textPropagator");

      propagators.add(textPropagator);
      return this;
    }

    /** Hello. */
    public StackPropagator build() {
      return new StackPropagator(propagators);
    }
  }
}
