/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.Entity;
import io.opentelemetry.api.incubator.entities.EntityBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.Collection;
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
  public boolean addOrUpdate(Entity e) {
    synchronized (writeLock) {
      Resource current = getResource();
      Resource next =
          EntityUtil.addEntity(Resource.builder(), ((PassthroughEntity) e).getPassthrough())
              .build();
      Resource result = current.merge(next);
      // We rely on a volatile read to force this to be written.
      resource.lazySet(result);
      return EntityUtil.getEntities(result).stream()
          .anyMatch(r -> r.getType().equals(e.getType()) && r.getId().equals(e.getId()));
    }
  }

  @Override
  public boolean removeEntity(Entity e) {
    synchronized (writeLock) {
      Resource current = getResource();
      Collection<io.opentelemetry.sdk.resources.internal.Entity> previousEntities =
          EntityUtil.getEntities(current);
      Collection<io.opentelemetry.sdk.resources.internal.Entity> currentEntities =
          new ArrayList<>(previousEntities);
      boolean result = currentEntities.removeIf(c -> c.getType().equals(e.getType()));
      ResourceBuilder rb = Resource.builder();
      EntityUtil.addAllEntity(rb, currentEntities);
      rb.putAll(
          current.getAttributes().toBuilder()
              .removeIf(
                  key ->
                      previousEntities.stream()
                          .anyMatch(
                              pe ->
                                  pe.getId().asMap().containsKey(key)
                                      || pe.getDescription().asMap().containsKey(key)))
              .build());
      resource.lazySet(rb.build());
      return result;
    }
  }

  @Override
  public EntityBuilder createEntity(String entityType) {
    return PassthroughEntity.builder(entityType);
  }
}
