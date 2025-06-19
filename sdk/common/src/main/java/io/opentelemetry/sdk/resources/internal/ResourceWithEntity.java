/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents a resource, which capture identifying information about the entities
 * for which signals (stats or traces) are reported.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public interface ResourceWithEntity {
  /**
   * Returns the URL of the OpenTelemetry schema used by this resource. May be null.
   *
   * @return An OpenTelemetry schema URL.
   * @since 1.4.0
   */
  @Nullable
  String getSchemaUrl();

  /**
   * Returns a map of attributes that describe the resource, not associated with an entity.
   *
   * @return a map of attributes.
   */
  Attributes getRawAttributes();

  /**
   * Returns a collectoion of associated entities.
   *
   * @return a collection of entities.
   */
  Collection<Entity> getEntities();

  /**
   * Returns a map of attributes that describe the resource.
   *
   * <p>Note: this includes all entity attribtues and raw attributes.
   *
   * @return a map of attributes.
   */
  Attributes getAttributes();

  /**
   * Returns a new {@link ResourceBuilder} instance populated with the data of this {@link
   * Resource}.
   */
  ResourceWithEntityBuilder toBuilder();

  // TODO - Merge
}
