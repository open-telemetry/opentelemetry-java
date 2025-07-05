/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

import io.opentelemetry.api.common.Attributes;

final class NoopEntity implements Entity {

  @Override
  public String getType() {
    return "";
  }

  @Override
  public Attributes getId() {
    return Attributes.empty();
  }

  @Override
  public Attributes getDescription() {
    return Attributes.empty();
  }

  @Override
  public String getSchemaUrl() {
    return "";
  }

  @Override
  public EntityBuilder toBuilder() {
    return new NoopEntityBuilder();
  }
}
