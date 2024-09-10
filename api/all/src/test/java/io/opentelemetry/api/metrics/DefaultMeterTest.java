/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.testing.internal.AbstractDefaultMeterTest;

public class DefaultMeterTest extends AbstractDefaultMeterTest {

  @Override
  protected Meter getMeter() {
    return DefaultMeter.getInstance();
  }

  @Override
  protected MeterProvider getMeterProvider() {
    return DefaultMeterProvider.getInstance();
  }
}
