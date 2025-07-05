/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

import io.opentelemetry.api.common.AttributesBuilder;
import java.util.function.Consumer;

final class NoopEntityBuilder implements EntityBuilder {
  @Override
  public EntityBuilder setSchemaUrl(String schemaUrl) {
    return this;
  }

  @Override
  public EntityBuilder withDescription(Consumer<AttributesBuilder> f) {
    return this;
  }

  @Override
  public EntityBuilder withId(Consumer<AttributesBuilder> f) {
    return this;
  }

  @Override
  public Entity build() {
    return new NoopEntity();
  }
}
