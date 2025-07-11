/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.internal.Entity;
import javax.annotation.Nullable;

final class SdkEntityState implements EntityState {
  private final Entity delegate;

  SdkEntityState(Entity delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getType() {
    return delegate.getType();
  }

  @Override
  @Nullable
  public String getSchemaUrl() {
    return delegate.getSchemaUrl();
  }

  @Override
  public Attributes getId() {
    return delegate.getId();
  }

  @Override
  public Attributes getDescription() {
    return delegate.getDescription();
  }
}
