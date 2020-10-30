/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class DefaultMeterProvider implements MeterProvider {

  private static final MeterProvider INSTANCE = new DefaultMeterProvider();

  static MeterProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public Meter get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Meter get(String instrumentationName, @Nullable String instrumentationVersion) {
    return Meter.getDefault();
  }

  private DefaultMeterProvider() {}
}
