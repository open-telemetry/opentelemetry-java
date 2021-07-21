/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.propagation;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

/**
 * A {@link TextMapPropagator} which can be configured with a set of fields, which will be extracted
 * and stored in {@link Context}. If the {@link Context} is used again to inject, the values will be
 * injected as-is. This {@link TextMapPropagator} is appropriate for a service that does not need to
 * participate in telemetry in any way and provides the most efficient way of propagating incoming
 * context to outgoing requests. In almost all cases, you will configure this single {@link
 * TextMapPropagator} when using {@link
 * io.opentelemetry.api.OpenTelemetry#propagating(ContextPropagators)} to create an {@link
 * io.opentelemetry.api.OpenTelemetry} that only propagates. Similarly, you will never need this
 * when using the OpenTelemetry SDK to enable telemetry.
 */
public final class PassThroughPropagator implements TextMapPropagator {

  private static final ContextKey<List<String>> EXTRACTED_KEY_VALUES =
      ContextKey.named("passthroughpropagator-keyvalues");

  private final List<String> fields;

  private PassThroughPropagator(List<String> fields) {
    this.fields = Collections.unmodifiableList(fields);
  }

  /**
   * Returns a {@link TextMapPropagator} which will propagate the given {@code fields} from
   * extraction to injection.
   */
  public static TextMapPropagator create(String... fields) {
    requireNonNull(fields, "fields");
    return create(Arrays.asList(fields));
  }

  /**
   * Returns a {@link TextMapPropagator} which will propagate the given {@code fields} from
   * extraction to injection.
   */
  public static TextMapPropagator create(Iterable<String> fields) {
    requireNonNull(fields, "fields");
    List<String> fieldsList =
        StreamSupport.stream(fields.spliterator(), false)
            .map(field -> requireNonNull(field, "field"))
            .collect(Collectors.toList());
    if (fieldsList.isEmpty()) {
      return TextMapPropagator.noop();
    }
    return new PassThroughPropagator(fieldsList);
  }

  @Override
  public Collection<String> fields() {
    return fields;
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    List<String> extracted = context.get(EXTRACTED_KEY_VALUES);
    if (extracted != null) {
      for (int i = 0; i < extracted.size(); i += 2) {
        setter.set(carrier, extracted.get(i), extracted.get(i + 1));
      }
    }
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    List<String> extracted = null;
    for (String field : fields) {
      String value = getter.get(carrier, field);
      if (value != null) {
        if (extracted == null) {
          extracted = new ArrayList<>();
        }
        extracted.add(field);
        extracted.add(value);
      }
    }
    return extracted != null ? context.with(EXTRACTED_KEY_VALUES, extracted) : context;
  }
}
