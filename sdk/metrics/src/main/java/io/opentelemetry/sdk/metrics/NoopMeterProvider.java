/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;

/** Implementation of {@link MeterProvider} which does not record or emit metrics. */
final class NoopMeterProvider implements MeterProvider {

  @Override
  public MeterBuilder meterBuilder(String instrumentationName) {
    return MeterProvider.noop().meterBuilder(instrumentationName);
  }
}
