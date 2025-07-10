/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

final class NoopResource implements Resource {

  static final Resource INSTANCE = new NoopResource();

  @Override
  public boolean removeEntity(String entityType) {
    return false;
  }

  @Override
  public EntityBuilder attachEntity(String entityType) {
    return NoopEntityBuilder.INSTANCE;
  }
}
