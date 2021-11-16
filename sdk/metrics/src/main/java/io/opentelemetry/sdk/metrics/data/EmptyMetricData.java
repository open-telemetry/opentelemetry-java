/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;

final class EmptyMetricData implements MetricData {

  static final EmptyMetricData INSTANCE = new EmptyMetricData();

  private EmptyMetricData() {}

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
