/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;

/** Implementation of SdkMeterProvider which does not collect metrics. */
// Visible for testing.
public final class NoopSdkMeterProvider implements SdkMeterProvider {

  @Override
  public MeterBuilder meterBuilder(String instrumentationName) {
    return MeterProvider.noop().meterBuilder(instrumentationName);
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
