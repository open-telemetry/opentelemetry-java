/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.metrics;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class DefaultMeterProvider implements MeterProvider {

  private static final MeterProvider instance = new DefaultMeterProvider();

  /**
   * Returns a {@code MeterProvider} singleton that is the default implementation for {@link
   * MeterProvider}.
   *
   * @return a {@code MeterProvider} singleton that is the default implementation for {@link
   *     MeterProvider}.
   */
  public static MeterProvider getInstance() {
    return instance;
  }

  @Override
  public Meter get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  @Override
  public Meter get(String instrumentationName, String instrumentationVersion) {
    return DefaultMeter.getInstance();
  }

  private DefaultMeterProvider() {}
}
