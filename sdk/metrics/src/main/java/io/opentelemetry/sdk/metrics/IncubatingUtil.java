/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;

/**
 * Utilities for interacting with {@code io.opentelemetry:opentelemetry-api-incubator}, which is not
 * guaranteed to be present on the classpath. For all methods, callers MUST first separately
 * reflectively confirm that the incubator is available on the classpath.
 */
final class IncubatingUtil {

  private IncubatingUtil() {}

  static LongCounterBuilder createExtendedLongCounterBuilder(SdkMeter sdkMeter, String name) {
    return new ExtendedSdkLongCounter.ExtendedSdkLongCounterBuilder(sdkMeter, name);
  }

  static LongUpDownCounterBuilder createExtendedLongUpDownCounterBuilder(
      SdkMeter sdkMeter, String name) {
    return new ExtendedSdkLongUpDownCounter.ExtendedSdkLongUpDownCounterBuilder(sdkMeter, name);
  }

  static DoubleHistogramBuilder createExtendedDoubleHistogramBuilder(
      SdkMeter sdkMeter, String name) {
    return new ExtendedSdkDoubleHistogram.ExtendedSdkDoubleHistogramBuilder(sdkMeter, name);
  }

  static DoubleGaugeBuilder createExtendedDoubleGaugeBuilder(SdkMeter sdkMeter, String name) {
    return new ExtendedSdkDoubleGauge.ExtendedSdkDoubleGaugeBuilder(sdkMeter, name);
  }
}
