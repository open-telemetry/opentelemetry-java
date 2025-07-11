/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

import io.opentelemetry.api.common.Attributes;

final class NoopEntityBuilder implements EntityBuilder {

  static final EntityBuilder INSTANCE = new NoopEntityBuilder();

  @Override
  public EntityBuilder setSchemaUrl(String schemaUrl) {
    return this;
  }

  @Override
  public EntityBuilder withDescription(Attributes description) {
    return this;
  }

  @Override
  public EntityBuilder withId(Attributes id) {
    return this;
  }

  @Override
  public void emit() {}
}
