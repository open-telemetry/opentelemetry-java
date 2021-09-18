/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;

/** Configuration representing no aggregation. */
class NoAggregation extends Aggregation {

  NoAggregation() {}

  @Override
  public <T> Aggregator<T> createAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      ExemplarFilter exemplarFilter) {
    return null;
  }

  @Override
  public String toString() {
    return "none";
  }
}
