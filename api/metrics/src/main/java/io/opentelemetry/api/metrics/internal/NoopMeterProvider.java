/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.internal;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

// TODO: Figure out real behavior for this.
public class NoopMeterProvider implements MeterProvider {
  @Override
  public Meter get(String instrumentationName, String instrumentationVersion, String schemaUrl) {
    return NoopMeter.getInstance();
  }

  private static final NoopMeterProvider INSTANCE = new NoopMeterProvider();

  public static MeterProvider getInstance() {
    return INSTANCE;
  }

  private NoopMeterProvider() {}
}
