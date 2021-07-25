/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/**
 * Exemplar filters are used to pre-filter measurements before attempting to store them in a
 * resorvior.
 */
public interface ExemplarFilter {
  /** Returns whether or not a resorvoir should attempt to filter a measurement. */
  boolean shouldSampleLongMeasurement(long value, Attributes attributes, Context context);

  /** Returns whether or not a resorvoir should attempt to filter a measurement. */
  boolean shouldSampleDoubleMeasurement(double value, Attributes attributes, Context context);

  /** A filter that only allows exemplars with traces. */
  public static final ExemplarFilter WITH_TRACES = new WithTraceExemplarFilter();
  /** A filter that accepts any measurement. */
  public static final ExemplarFilter ALWAYS_ON =
      new ExemplarFilter() {

        @Override
        public boolean shouldSampleLongMeasurement(
            long value, Attributes attributes, Context context) {
          return true;
        }

        @Override
        public boolean shouldSampleDoubleMeasurement(
            double value, Attributes attributes, Context context) {
          return true;
        }
      };
  /** A filter that accepts no measurements. */
  public static final ExemplarFilter ALWAYS_OFF =
      new ExemplarFilter() {

        @Override
        public boolean shouldSampleLongMeasurement(
            long value, Attributes attribtues, Context context) {
          return false;
        }

        @Override
        public boolean shouldSampleDoubleMeasurement(
            double value, Attributes attribtues, Context context) {
          return false;
        }
      };
}
