/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.correlationcontext.spi;

import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.spi.CorrelationContextManagerFactory;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;

/**
 * {@code CorrelationContextManager} provider implementation for {@link
 * CorrelationContextManagerFactory}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.OpenTelemetry}.
 */
public final class CorrelationContextManagerFactorySdk implements CorrelationContextManagerFactory {

  @Override
  public CorrelationContextManager create() {
    return new CorrelationContextManagerSdk();
  }
}
