/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.trace.ContextHandle;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import javax.annotation.Nullable;

class OpenTelemetryCtx implements ContextHandle {

  private final Context context;

  @Nullable
  private Scope scope;

  public OpenTelemetryCtx(Context context) {
    this.context = context;
  }

  Context getContext() {
    return context;
  }

  @Override
  @SuppressWarnings("MustBeClosedChecker")
  public ContextHandle attach() {
    scope = context.makeCurrent();
    return this;
  }

  @Override
  public void detach(ContextHandle ctx) {
    OpenTelemetryCtx impl = (OpenTelemetryCtx) ctx;
    if (impl.scope != null) {
      impl.scope.close();
    }
  }
}
