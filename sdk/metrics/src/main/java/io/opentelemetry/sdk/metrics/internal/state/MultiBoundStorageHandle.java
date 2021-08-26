/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.List;

/** Storage handle that aggregates across several instances. */
class MultiBoundStorageHandle implements BoundStorageHandle {
  private final List<BoundStorageHandle> underlyingHandles;

  MultiBoundStorageHandle(List<BoundStorageHandle> handles) {
    this.underlyingHandles = handles;
  }

  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    for (BoundStorageHandle handle : underlyingHandles) {
      handle.recordLong(value, attributes, context);
    }
  }

  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    for (BoundStorageHandle handle : underlyingHandles) {
      handle.recordDouble(value, attributes, context);
    }
  }

  @Override
  public void release() {
    for (BoundStorageHandle handle : underlyingHandles) {
      handle.release();
    }
  }
}
