/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.sdk.internal.DynamicList;
import io.opentelemetry.sdk.internal.ObjectPool;

public class MarshallingObjectsPool {
  private final ObjectPool<DynamicList<MessageSize>> messageSizeDynamicListPool =
      new ObjectPool<>(DynamicList::new);
  private final ObjectPool<DefaultMessageSize> defaultMessageSizePool =
      new ObjectPool<>(DefaultMessageSize::new);

  // Accepts listSize to enable choosing the right size reusable list from the pool
  // in the future
  public DynamicList<MessageSize> borrowDynamicList(int listSize) {
    DynamicList<MessageSize> dynamicList = messageSizeDynamicListPool.borrowObject();
    dynamicList.resizeAndClear(listSize);
    return dynamicList;
  }

  public void returnDynamicList(DynamicList<MessageSize> dynamicList) {
    messageSizeDynamicListPool.returnObject(dynamicList);
  }

  public ObjectPool<DefaultMessageSize> getDefaultMessageSizePool() {
    return defaultMessageSizePool;
  }
}
