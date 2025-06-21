/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities.detectors;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.extension.incubator.entities.Entity;
import io.opentelemetry.sdk.extension.incubator.entities.EntityDetector;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Detection for {@code service} entity.
 *
 * <p>See: <a href="https://opentelemetry.io/docs/specs/semconv/resource/#service">service
 * entity</a>
 */
public final class ServiceDetector implements EntityDetector {
  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.34.0";
  private static final String ENTITY_TYPE = "service";
  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
  private static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");
  private static final UUID FALLBACK_INSTANCE_ID = UUID.randomUUID();

  private static String getServiceName() {
    return System.getenv().getOrDefault("OTEL_SERVICE_NAME", "unknown_service:java");
  }

  private static String getServiceInstanceId() {
    // TODO - no way for users to specify a non-default.
    return FALLBACK_INSTANCE_ID.toString();
  }

  @Override
  public Collection<Entity> detect() {
    return Collections.singletonList(
        Entity.builder(ENTITY_TYPE)
            .setSchemaUrl(SCHEMA_URL)
            .withId(
                id -> {
                  // Note: Identifying attributes MUST be provided together.
                  id.put(SERVICE_NAME, getServiceName())
                      .put(SERVICE_INSTANCE_ID, getServiceInstanceId());
                })
            // No specified way to take these in.
            // .withDescriptive(
            //     builder -> {
            //       if (!StringUtils.isNullOrEmpty(getVersion())) {
            //         builder.put(SERVICE_VERSION, getVersion());
            //       }
            //     })
            .build());
  }
}
