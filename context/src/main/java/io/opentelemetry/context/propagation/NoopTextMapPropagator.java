/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import io.opentelemetry.context.Context;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

final class NoopTextMapPropagator implements TextMapPropagator {
  private static final NoopTextMapPropagator INSTANCE = new NoopTextMapPropagator();

  static TextMapPropagator getInstance() {
    return INSTANCE;
  }

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
