/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.detectors;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.Entity;
import io.opentelemetry.sdk.resources.EntityDetector;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ServiceInstanceDetector implements EntityDetector {
  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.28.0";
  private static final String ENTITY_TYPE = "service.instance";
  private static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  private static final UUID FALLBACK_INSTANCE_ID = UUID.randomUUID();

  private static String getInstanceId() {
    // Note: As specified by semantic conventions.
    return FALLBACK_INSTANCE_ID.toString();
  }

  @Override
  public List<Entity> detectEntities() {
    return Collections.singletonList(
        Entity.builder(ENTITY_TYPE)
            .setSchemaUrl(SCHEMA_URL)
            .withIdentifying(
                builder -> {
                  // Note: Identifying attributes MUST be provided together.
                  builder.put(SERVICE_INSTANCE_ID, getInstanceId());
                })
            .build());
  }

  private ServiceInstanceDetector() {}

  public static final EntityDetector INSTANCE = new ServiceInstanceDetector();
}
