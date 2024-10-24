/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation.internal;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;

/**
 * Declarative configuration SPI implementation for {@link B3Propagator} which allows enables the
 * {@link B3Propagator#injectingMultiHeaders()}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class B3MultiComponentProvider implements ComponentProvider<TextMapPropagator> {

  @Override
  public Class<TextMapPropagator> getType() {
    return TextMapPropagator.class;
  }

  @Override
  public String getName() {
    return "b3multi";
  }

  @Override
  public TextMapPropagator create(StructuredConfigProperties config) {
    return B3Propagator.injectingMultiHeaders();
  }
}
