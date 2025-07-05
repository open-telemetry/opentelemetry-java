/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

final class NoopResource implements Resource {

  @Override
  public boolean addOrUpdate(Entity e) {
    return false;
  }

  @Override
  public boolean removeEntity(Entity e) {
    return false;
  }

  @Override
  public EntityBuilder createEntity(String entityType) {
    return new NoopEntityBuilder();
  }
}
