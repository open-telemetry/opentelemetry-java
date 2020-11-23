/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@Deprecated
public final class HttpTraceContext implements TextMapPropagator {
  private static final HttpTraceContext INSTANCE = new HttpTraceContext();

  private HttpTraceContext() {
    // singleton
  }

  public static HttpTraceContext getInstance() {
    return INSTANCE;
  }

  @Override
  public List<String> fields() {
    return W3CTraceContextPropagator.getInstance().fields();
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, Setter<C> setter) {
    W3CTraceContextPropagator.getInstance().inject(context, carrier, setter);
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter) {
    return W3CTraceContextPropagator.getInstance().extract(context, carrier, getter);
  }
}
