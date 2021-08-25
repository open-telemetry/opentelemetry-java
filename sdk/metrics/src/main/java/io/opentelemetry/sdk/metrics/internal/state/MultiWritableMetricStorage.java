/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.List;

class MultiWritableMetricStorage implements WriteableMetricStorage {
  private final List<WriteableMetricStorage> underlyingMetrics;

  MultiWritableMetricStorage(List<WriteableMetricStorage> metrics) {
    this.underlyingMetrics = metrics;
  }

  @Override
  public BoundStorageHandle bind(Attributes attributes) {
    List<BoundStorageHandle> handles = new ArrayList<>(underlyingMetrics.size());
    for (WriteableMetricStorage metric : underlyingMetrics) {
      handles.add(metric.bind(attributes));
    }
    return new MultiBoundStorageHandle(handles);
  }
}
