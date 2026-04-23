/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import io.opentelemetry.common.ApiUsageLogger;
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
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
    if (context == null) {
      ApiUsageLogger.logNullParam(TextMapPropagator.class, "inject", "context");
      return;
    }
    if (setter == null) {
      ApiUsageLogger.logNullParam(TextMapPropagator.class, "inject", "setter");
      return;
    }
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (context == null) {
      ApiUsageLogger.logNullParam(TextMapPropagator.class, "extract", "context");
      return Context.root();
    }
    if (getter == null) {
      ApiUsageLogger.logNullParam(TextMapPropagator.class, "extract", "getter");
      return context;
    }
    return context;
  }

  @Override
  public String toString() {
    return "NoopTextMapPropagator";
  }
}
