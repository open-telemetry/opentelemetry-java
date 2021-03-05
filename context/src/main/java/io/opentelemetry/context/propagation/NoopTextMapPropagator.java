/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import io.opentelemetry.context.Context;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

final class NoopTextMapPropagator implements TextMapPropagator {
  private static final NoopTextMapPropagator INSTANCE = new NoopTextMapPropagator();

  static TextMapPropagator getInstance() {
    return INSTANCE;
  }

  @Override
  public Collection<String> fields() {
    return Collections.emptyList();
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {}

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    return context;
  }
}
