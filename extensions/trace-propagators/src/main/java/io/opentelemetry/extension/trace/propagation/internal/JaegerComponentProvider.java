/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;

/**
 * Declarative configuration SPI implementation for {@link JaegerPropagator}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JaegerComponentProvider implements ComponentProvider {

  @Override
  public Class<TextMapPropagator> getType() {
    return TextMapPropagator.class;
  }

  @Override
  public String getName() {
    return "jaeger";
  }

  @Override
  public TextMapPropagator create(DeclarativeConfigProperties config) {
    return JaegerPropagator.getInstance();
  }
}
