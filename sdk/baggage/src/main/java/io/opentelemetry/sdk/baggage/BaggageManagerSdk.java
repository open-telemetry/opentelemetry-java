/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage;

import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.context.Scope;

/** {@link BaggageManagerSdk} is SDK implementation of {@link BaggageManager}. */
public class BaggageManagerSdk implements BaggageManager {

  @Override
  public Baggage getCurrentBaggage() {
    return BaggageUtils.getCurrentBaggage();
  }

  @Override
  public Baggage.Builder baggageBuilder() {
    return new BaggageSdk.Builder();
  }

  @Override
  public Scope withBaggage(Baggage baggage) {
    return BaggageUtils.currentContextWith(baggage);
  }
}
