/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

public class MeterProviderImpl implements MeterProvider {
  private final Accumulator accumulator;

  public MeterProviderImpl(Accumulator accumulator) {
    this.accumulator = accumulator;
  }

  public static MeterProvider create(Clock clock) {
    return new MeterProviderImpl(new Accumulator(clock));
  }

  @Override
  public Meter get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Meter get(String instrumentationName, String instrumentationVersion) {
    // todo: cache/register/lookup to avoid creating extras.
    return new MeterImpl(
        accumulator,
        InstrumentationLibraryInfo.create(instrumentationName, instrumentationVersion));
  }
}
