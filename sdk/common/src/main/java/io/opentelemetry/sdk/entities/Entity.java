/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.entities;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class Entity {

  public static Entity create(String id, String name, Attributes attributes) {
    return new AutoValue_Entity(id, name, attributes);
  }

  public static Entity create(Resource resource) {
    // TODO: We didn't have an id/name, so we just use the hashCode() and this feels hacky
    return create(
        String.valueOf(resource.hashCode()),
        String.valueOf(resource.hashCode()),
        resource.getAttributes());
  }

  public abstract String getId();

  public abstract String getName();

  public abstract Attributes getAttributes();

  public Entity withAttributes(Attributes newAttributes) {
    return new AutoValue_Entity(getId(), getName(), newAttributes);
  }
}
