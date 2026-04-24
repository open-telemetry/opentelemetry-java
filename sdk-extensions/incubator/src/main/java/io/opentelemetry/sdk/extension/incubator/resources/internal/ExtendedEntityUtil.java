/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.extension.incubator.resources.Entity;
import io.opentelemetry.sdk.extension.incubator.resources.EntityDetector;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.EntityBuilder;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collection;

/**
 * This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class ExtendedEntityUtil {
  private ExtendedEntityUtil() {}

  /** Convert between the incubator API entity and the internal-implementation SDK entity. */
  static io.opentelemetry.sdk.resources.internal.Entity convertEntity(Entity entity) {
    SdkEntity api = (SdkEntity) entity;
    EntityBuilder builder = io.opentelemetry.sdk.resources.internal.Entity.builder(api.getType());
    if (api.getSchemaUrl() != null) {
      builder.setSchemaUrl(api.getSchemaUrl());
    }
    builder.withId(api.getIdentity());
    builder.withDescription(api.getDescription());
    return builder.build();
  }

  /** Runs a set of EntityDetectors (in priority order) and merges the results into a Resource. */
  public static Resource runDetection(
      Collection<EntityDetector> detectors, ConfigProperties config) {
    ResourceBuilder builder = Resource.builder();
    for (EntityDetector detector : detectors) {
      for (Entity entity : detector.detect(config)) {
        if (entity != null) {
          EntityUtil.addEntity(builder, convertEntity(entity));
        }
      }
    }
    return builder.build();
  }
}
