/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * SDK implementation of Entity.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
abstract class SdkEntity implements Entity {
  /**
   * Returns a {@link Entity}.
   *
   * @param entityType the entity type string of this entity.
   * @param id a map of attributes that identify the entity.
   * @param description a map of attributes that describe the entity.
   * @return a {@code Entity}.
   * @throws NullPointerException if {@code id} or {@code description} is null.
   * @throws IllegalArgumentException if entityType string, attribute key or attribute value is not
   *     a valid printable ASCII string or exceed {@link AttributeCheckUtil#MAX_LENGTH} characters.
   */
  static final Entity create(
      String entityType, Attributes id, Attributes description, @Nullable String schemaUrl) {
    AttributeCheckUtil.isValid(entityType);
    AttributeCheckUtil.checkAttributes(id);
    AttributeCheckUtil.checkAttributes(description);
    return new AutoValue_SdkEntity(entityType, id, description, schemaUrl);
  }

  @Override
  public final EntityBuilder toBuilder() {
    return new SdkEntityBuilder(this);
  }

  /**
   * Returns a new {@link EntityBuilder} instance for creating arbitrary {@link Entity}.
   *
   * @param entityType the entity type string of this entity.
   */
  public static final EntityBuilder builder(String entityType) {
    return new SdkEntityBuilder(entityType);
  }
}
