/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.EntityBuilder;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class SdkResource implements io.opentelemetry.api.incubator.entities.Resource {

  // The currently advertised Resource to other SDK providers.
  private final AtomicReference<Resource> resource = new AtomicReference<>(Resource.empty());
  private final Object writeLock = new Object();

  // Our internal storage of registered entities.
  @GuardedBy("writeLock")
  private final ArrayList<Entity> entities = new ArrayList<>();

  private static final Logger logger = Logger.getLogger(SdkResource.class.getName());

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

  private static boolean hasSameSchemaUrl(Entity lhs, Entity rhs) {
    if (lhs.getSchemaUrl() != null) {
      return lhs.getSchemaUrl().equals(rhs.getSchemaUrl());
    }
    return rhs.getSchemaUrl() == null;
  }

  void attachEntityOnEmit(Entity e) {
    synchronized (writeLock) {
      @Nullable Entity conflict = null;
      for (Entity existing : entities) {
        if (existing.getType().equals(e.getType())) {
          conflict = existing;
        }
      }

      if (conflict != null) {
        if (hasSameSchemaUrl(conflict, e) && conflict.getId().equals(e.getId())) {
          // We can merge descriptive attributes.
          entities.remove(conflict);
          io.opentelemetry.sdk.resources.internal.EntityBuilder newEntity =
              Entity.builder(conflict.getType())
                  .withId(conflict.getId())
                  .withDescription(
                      conflict.getDescription().toBuilder().putAll(e.getDescription()).build());
          if (conflict.getSchemaUrl() != null) {
            newEntity.setSchemaUrl(conflict.getSchemaUrl());
          }
          entities.add(newEntity.build());
        } else {
          // TODO - use ThrottlingLogger?
          logger.log(Level.WARNING, "Ignoring new entity, conflicts with existing: ", e);
        }
      } else {
        entities.add(e);
      }

      resource.lazySet(EntityUtil.createResource(entities));
    }
  }

  @Override
  public EntityBuilder attachEntity(String entityType) {
    return new SdkEntityBuilder(entityType, this::attachEntityOnEmit);
  }
}
