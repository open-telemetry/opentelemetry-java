/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources.internal;

import io.opentelemetry.sdk.extension.incubator.resources.Entity;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.EntityBuilder;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class ExtendedEntityUtil {
  private ExtendedEntityUtil() {}

  /** Convert between the incubator API entity and the internal-implementation SDK entity. */
  public static io.opentelemetry.sdk.resources.internal.Entity convertEntity(Entity entity) {
    SdkEntity api = (SdkEntity) entity;
    EntityBuilder builder = io.opentelemetry.sdk.resources.internal.Entity.builder(api.getType());
    if (api.getSchemaUrl() != null) {
      builder.setSchemaUrl(api.getSchemaUrl());
    }
    builder.withId(api.getIdentity());
    builder.withDescription(api.getDescription());
    return builder.build();
  }

  /** Constructs a resource from a prioritized list of entities. */
  public static Resource createResource(Collection<Entity> entities) {
    return EntityUtil.createResource(
        entities.stream().map(ExtendedEntityUtil::convertEntity).collect(Collectors.toList()));
  }
}
