/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Exemplar filters are used to pre-filter measurements before attempting to store them in a
 * reservoir.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExemplarFilter {
  /** Returns whether or not a reservoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(long value, Attributes attributes, Context context);

  /** Returns whether or not a reservoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(double value, Attributes attributes, Context context);

  /**
   * A filter that only accepts measurements where there is a {@code Span} in {@link Context} that
   * is being sampled.
   */
  static ExemplarFilter sampleWithTraces() {
    return WithTraceExemplarFilter.INSTANCE;
  }

  /** A filter that accepts any measurement. */
  static ExemplarFilter alwaysSample() {
    return AlwaysSampleFilter.INSTANCE;
  }

  /** A filter that accepts no measurements. */
  static ExemplarFilter neverSample() {
    return NeverSampleFilter.INSTANCE;
  }

  /**
   * Reflectively assign the {@link ExemplarFilter} to the {@link SdkMeterProviderBuilder}.
   *
   * @param sdkMeterProviderBuilder the
   */
  static void setExemplarFilter(
      SdkMeterProviderBuilder sdkMeterProviderBuilder, ExemplarFilter exemplarFilter) {
    try {
      Method method =
          SdkMeterProviderBuilder.class.getDeclaredMethod(
              "setExemplarFilter", ExemplarFilter.class);
      method.setAccessible(true);
      method.invoke(sdkMeterProviderBuilder, exemplarFilter);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setExemplarFilter on SdkMeterProviderBuilder", e);
    }
  }
}
