/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.List;

class MultiWritableMetricStorage implements WriteableMetricStorage {
  private final List<? extends WriteableMetricStorage> storages;

  MultiWritableMetricStorage(List<? extends WriteableMetricStorage> storages) {
    this.storages = storages;
  }

  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    for (WriteableMetricStorage storage : storages) {
      storage.recordLong(value, attributes, context);
    }
  }

  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    for (WriteableMetricStorage storage : storages) {
      storage.recordDouble(value, attributes, context);
    }
  }
}
