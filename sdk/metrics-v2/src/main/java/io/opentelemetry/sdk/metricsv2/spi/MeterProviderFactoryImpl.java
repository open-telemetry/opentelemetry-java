/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2.spi;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.internal.SystemClock;
import io.opentelemetry.sdk.metricsv2.MeterProviderImpl;
import io.opentelemetry.spi.metrics.MeterProviderFactory;

public class MeterProviderFactoryImpl implements MeterProviderFactory {
  @Override
  public MeterProvider create() {
    return MeterProviderImpl.create(SystemClock.getInstance());
  }
}
