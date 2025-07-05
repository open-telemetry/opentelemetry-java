/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.EntityBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.concurrent.atomic.AtomicReference;

final class SdkResource implements io.opentelemetry.api.incubator.entities.Resource {

  private final AtomicReference<Resource> resource = new AtomicReference<>(Resource.empty());
  private final Object writeLock = new Object();

  /** Returns the currently active resource. */
  public Resource getResource() {
    Resource result = resource.get();
    // We do this to make NullAway happy.
    if (result == null) {
      throw new IllegalStateException("SdkResource should never have null resource");
    }
    return result;
  }

  @Override
  public boolean removeEntity(String entityType) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'removeEntity'");
  }

  void attachEntityOnEmit(Entity e) {
    synchronized (writeLock) {
      Resource current = getResource();
      Resource next = EntityUtil.addEntity(Resource.builder(), e).build();
      resource.lazySet(current.merge(next));
    }
  }

  @Override
  public EntityBuilder attachEntity(String entityType) {
    return new SdkEntityBuilder(entityType, this::attachEntityOnEmit);
  }
}
