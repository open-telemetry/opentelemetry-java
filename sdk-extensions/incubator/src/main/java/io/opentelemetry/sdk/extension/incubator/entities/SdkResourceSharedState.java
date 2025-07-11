/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * This class does all state and listener management for a {@link Resource} constructed of {@link
 * Entity}s.
 */
final class SdkResourceSharedState {

  // The currently advertised Resource to other SDK providers.
  private final AtomicReference<Resource> resource = new AtomicReference<>(Resource.empty());
  private final Object writeLock = new Object();
  private final List<EntityListener> listeners = new CopyOnWriteArrayList<>();
  private final ExecutorService listenerExecutor;

  // Our internal storage of registered entities.
  @GuardedBy("writeLock")
  private final ArrayList<Entity> entities = new ArrayList<>();

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(SdkResourceSharedState.class.getName()));

  SdkResourceSharedState(ExecutorService listenerExecutor) {
    this.listenerExecutor = listenerExecutor;
  }

  /**
   * Shutdown the provider. The resulting {@link CompletableResultCode} completes when all complete.
   */
  CompletableResultCode shutdown() {
    // TODO - Actually figure out how to wait for shutdown and deal with pending tasks.
    listenerExecutor.shutdown();
    return CompletableResultCode.ofSuccess();
  }

  /** Returns the currently active resource. */
  public Resource getResource() {
    Resource result = resource.get();
    // We do this to make NullAway happy.
    if (result == null) {
      throw new IllegalStateException("SdkResource should never have null resource");
    }
    return result;
  }

  private static boolean hasSameSchemaUrl(Entity lhs, Entity rhs) {
    if (lhs.getSchemaUrl() != null) {
      return lhs.getSchemaUrl().equals(rhs.getSchemaUrl());
    }
    return rhs.getSchemaUrl() == null;
  }

  /**
   * Removes an entity by type and notifies listeners.
   *
   * @param entityType The entity type to remove.
   */
  boolean removeEntity(String entityType) {
    synchronized (writeLock) {
      @Nullable Entity removed = null;
      for (Entity existing : entities) {
        if (existing.getType().equals(entityType)) {
          removed = existing;
        }
      }
      if (removed == null) {
        return false;
      }
      entities.remove(removed);
      Resource result = EntityUtil.createResource(entities);
      resource.lazySet(result);
      publishEntityDelete(new SdkEntityState(removed), result);
      return true;
    }
  }

  /**
   * Adds an entity and notifies listeners.
   *
   * <p>Note: This will not add an entity on conflict. This will update the description if the
   * entity already exists.
   *
   * @param e The entity type to add.
   */
  void addOrUpdateEntity(Entity e) {
    synchronized (writeLock) {
      @Nullable Entity conflict = null;
      for (Entity existing : entities) {
        if (existing.getType().equals(e.getType())) {
          conflict = existing;
        }
      }
      Entity newState = e;
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
          newState = newEntity.build();
          entities.add(newState);
        } else {
          logger.log(Level.INFO, "Ignoring new entity, conflicts with existing: " + e);
          return;
        }
      } else {
        entities.add(e);
      }
      Resource result = EntityUtil.createResource(entities);
      resource.lazySet(result);
      publishEntityStateChange(new SdkEntityState(newState), result);
    }
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  private void publishEntityStateChange(EntityState state, Resource resource) {
    for (EntityListener listener : listeners) {
      // We isolate listener execution via executor, if configured.
      // We ignore failures on futures to avoid having one listener block others.
      listenerExecutor.submit(() -> listener.onEntityState(state, resource));
    }
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  private void publishEntityDelete(EntityState deleted, Resource resource) {
    for (EntityListener listener : listeners) {
      // We isolate listener execution via executor, if configured.
      // We ignore failures on futures to avoid having one listener block others.
      listenerExecutor.submit(() -> listener.onEntityDelete(deleted, resource));
    }
  }

  public void addListener(EntityListener listener) {
    listeners.add(listener);
  }
}
