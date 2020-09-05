/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.correlationcontext;

import io.opentelemetry.context.Scope;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.CorrelationsContextUtils;

/**
 * {@link CorrelationContextManagerSdk} is SDK implementation of {@link CorrelationContextManager}.
 */
public class CorrelationContextManagerSdk implements CorrelationContextManager {

  @Override
  public CorrelationContext getCurrentContext() {
    return CorrelationsContextUtils.getCurrentCorrelationContext();
  }

  @Override
  public CorrelationContext.Builder contextBuilder() {
    return new CorrelationContextSdk.Builder();
  }

  @Override
  public Scope withContext(CorrelationContext distContext) {
    return CorrelationsContextUtils.currentContextWith(distContext);
  }
}
