/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class DefaultMeterProvider implements MeterProvider {

  private static final MeterProvider instance = new DefaultMeterProvider();

  static MeterProvider getInstance() {
    return instance;
  }

  @Override
  public Meter get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Meter get(String instrumentationName, String instrumentationVersion) {
    return Meter.noop();
  }

  private DefaultMeterProvider() {}
}
