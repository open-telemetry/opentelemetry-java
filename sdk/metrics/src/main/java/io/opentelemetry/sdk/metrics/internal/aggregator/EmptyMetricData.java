/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.resources.Resource;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class EmptyMetricData implements MetricData {

  private static final EmptyMetricData INSTANCE = new EmptyMetricData();

  private EmptyMetricData() {}

  /**
   * Get an empty instance of metric data. Empty metric data should not be used except as an
   * indication that recordings were dropped via {@link Aggregation#drop()}.
   *
   * @return an empty MetricData.
   */
  public static MetricData getInstance() {
    return INSTANCE;
  }

  @Override
  public Resource getResource() {
    throw new UnsupportedOperationException("EmptyMetricData does not support getResource().");
  }

  @Override
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    throw new UnsupportedOperationException(
        "EmptyMetricData does not support getInstrumentationLibraryInfo().");
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException("EmptyMetricData does not support getName().");
  }

  @Override
  public String getDescription() {
    throw new UnsupportedOperationException("EmptyMetricData does not support getDescription().");
  }

  @Override
  public String getUnit() {
    throw new UnsupportedOperationException("EmptyMetricData does not support getUnit().");
  }

  @Override
  public MetricDataType getType() {
    throw new UnsupportedOperationException("EmptyMetricData does not support getType().");
  }

  @Override
  public Data<?> getData() {
    throw new UnsupportedOperationException("EmptyMetricData does not support getData().");
  }

  @Override
  public boolean isEmpty() {
    return true;
  }
}
