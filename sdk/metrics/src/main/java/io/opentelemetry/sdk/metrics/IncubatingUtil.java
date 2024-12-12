/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;

final class IncubatingUtil {

  private IncubatingUtil() {}

  static LongCounterBuilder createIncubatingLongCounterBuilder(SdkMeter sdkMeter, String name) {
    return new ExtendedSdkLongCounter.ExtendedSdkLongCounterBuilder(sdkMeter, name);
  }

  static LongUpDownCounterBuilder createIncubatingLongUpDownCounterBuilder(
      SdkMeter sdkMeter, String name) {
    return new ExtendedSdkLongUpDownCounter.ExtendedSdkLongUpDownCounterBuilder(sdkMeter, name);
  }

  static DoubleHistogramBuilder createIncubatingDoubleHistogramBuilder(
      SdkMeter sdkMeter, String name) {
    return new ExtendedSdkDoubleHistogram.ExtendedSdkDoubleHistogramBuilder(sdkMeter, name);
  }

  static DoubleGaugeBuilder createIncubatingDoubleGaugemBuilder(SdkMeter sdkMeter, String name) {
    return new ExtendedSdkDoubleGauge.ExtendedSdkDoubleGaugeBuilder(sdkMeter, name);
  }
}
