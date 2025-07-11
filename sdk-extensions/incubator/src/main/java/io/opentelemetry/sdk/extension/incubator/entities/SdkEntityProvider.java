/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.EntityBuilder;
import io.opentelemetry.api.incubator.entities.EntityProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.TimeUnit;

/** The SDK implementation of {@link EntityProvider}. */
public final class SdkEntityProvider implements EntityProvider {
  // TODO - Give control over listener execution model.
  // For now, just run everything on the same thread as the entity-attach call.
  private final SdkResourceSharedState state =
      new SdkResourceSharedState(new CurrentThreadExecutorService());

  /**
   * Obtains the current {@link Resource} for the SDK.
   *
   * @return the active {@link Resource} for this SDK.
   */
  public Resource getResource() {
    return state.getResource();
  }

  /**
   * Creates a builder for SdkEntityProvider.
   *
   * @return The new builder.
   */
  public static SdkEntityProviderBuilder builder() {
    return new SdkEntityProviderBuilder();
  }

  @Override
  public String toString() {
    return "SdkResourceProvider{}";
  }

  @Override
  public boolean removeEntity(String entityType) {
    return state.removeEntity(entityType);
  }

  @Override
  public EntityBuilder attachOrUpdateEntity(String entityType) {
    return new SdkEntityBuilder(entityType, state::addOrUpdateEntity);
  }

  public void onChange(EntityListener listener) {
    state.addListener(listener);
  }

  /**
   * Shutdown the provider. The resulting {@link CompletableResultCode} completes when all complete.
   */
  public CompletableResultCode shutdown() {
    return state.shutdown();
  }

  /** Close the provider. Calls {@link #shutdown()} and blocks waiting for it to complete. */
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
