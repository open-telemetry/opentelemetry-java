/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.EntityBuilder;
import io.opentelemetry.api.incubator.entities.EntityProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/** The SDK implementation of {@link EntityProvider}. */
public final class SdkEntityProvider implements EntityProvider {
  private final SdkResourceSharedState state;

  SdkEntityProvider(ExecutorService executorService, Collection<ResourceDetector> detectors) {
    this.state = new SdkResourceSharedState(executorService);
    state.beginInitialization(detectors, this);
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
