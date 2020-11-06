/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.spi;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.metrics.MeterProviderImpl;
import io.opentelemetry.spi.metrics.MeterProviderFactory;

public class MeterProviderFactoryImpl implements MeterProviderFactory {
  @Override
  public MeterProvider create() {
    return MeterProviderImpl.create();
  }
}
