/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;

/**
 * A mutable container of metrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class MutableMetricData implements MetricData {

  private final MetricDataType metricDataType;

  private Resource resource = Resource.getDefault();
  private InstrumentationScopeInfo scope = InstrumentationScopeInfo.empty();
  private String name = "";
  private String description = "";
  private String unit = "";
  private Data<?> data = (Data<PointData>) Collections::emptyList;

  public MutableMetricData(MetricDataType metricDataType) {
    this.metricDataType = metricDataType;
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  @Override
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return scope;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getUnit() {
    return unit;
  }

  @Override
  public MetricDataType getType() {
    return metricDataType;
  }

  @Override
  public Data<?> getData() {
    return data;
  }

  /** Set the values. */
  public void set(
      Resource resource,
      InstrumentationScopeInfo scope,
      String name,
      String description,
      String unit,
      Data<?> data) {
    this.resource = resource;
    this.scope = scope;
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.data = data;
  }
}
