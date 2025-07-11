/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

final class NoopEntityProvider implements EntityProvider {

  static final EntityProvider INSTANCE = new NoopEntityProvider();

  @Override
  public boolean removeEntity(String entityType) {
    return false;
  }

  @Override
  public EntityBuilder attachOrUpdateEntity(String entityType) {
    return NoopEntityBuilder.INSTANCE;
  }
}
