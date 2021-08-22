/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;

public abstract class AbstractAggregator<T> implements Aggregator<T> {
  private final Resource resource;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  private final MetricDescriptor metricDescriptor;
  private final boolean stateful;

  protected AbstractAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor metricDescriptor,
      boolean stateful) {
    this.resource = resource;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.metricDescriptor = metricDescriptor;
    this.stateful = stateful;
  }

  @Override
  public boolean isStateful() {
    return stateful;
  }

  protected final Resource getResource() {
    return resource;
  }

  protected final InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }

  protected final MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }
}
