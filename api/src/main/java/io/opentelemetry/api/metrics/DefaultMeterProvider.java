/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

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
  public Meter get(String instrumentationName, String instrumentationVersion) {
    Objects.requireNonNull(instrumentationName);
    return Meter.getDefault();
  }

  private DefaultMeterProvider() {}
}
