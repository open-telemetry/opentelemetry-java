/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.EntityBuilder;
import io.opentelemetry.api.incubator.entities.EntityProvider;
import io.opentelemetry.sdk.resources.Resource;

/** The SDK implementation of {@link EntityProvider}. */
public final class SdkEntityProvider implements EntityProvider {
  private final SdkResourceState state = new SdkResourceState();

  /**
   * Obtains the current {@link Resource} for the SDK.
   *
   * @return the active {@link Resource} for this SDK.
   */
  public Resource getResource() {
    return state.getResource();
  }

  public static SdkEntityProviderBuilder builder() {
    return new SdkEntityProviderBuilder();
  }

  @Override
  public String toString() {
    return "SdkResourceProvider{}";
  }

  @Override
  public boolean removeEntity(String entityType) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'removeEntity'");
  }

  @Override
  public EntityBuilder attachOrUpdateEntity(String entityType) {
    return new SdkEntityBuilder(entityType, state::attachEntityOnEmit);
  }
}
