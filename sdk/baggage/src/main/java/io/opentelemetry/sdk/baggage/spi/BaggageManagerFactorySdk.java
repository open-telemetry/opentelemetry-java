/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage.spi;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.spi.BaggageManagerFactory;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;

/**
 * {@code BaggageManager} provider implementation for {@link BaggageManagerFactory}.
 *
 * <p>This class is not intended to be used in application code and it is used only by {@link
 * io.opentelemetry.OpenTelemetry}.
 */
public final class BaggageManagerFactorySdk implements BaggageManagerFactory {

  @Override
  public BaggageManager create() {
    return new BaggageManagerSdk();
  }
}
