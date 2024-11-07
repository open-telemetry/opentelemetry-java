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

/** Detects the `service` entity. */
public final class ServiceDetector implements EntityDetector {

  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.28.0";
  private static final String ENTITY_TYPE = "service";
  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

  // Disabled as this would be break integration tests.
  // private static final AttributeKey<String> SERVICE_NAMESPACE =
  //     AttributeKey.stringKey("service.namespace");
  //
  // private static final AttributeKey<String> SERVICE_VERSION =
  //     AttributeKey.stringKey("service.version");
  //
  //
  // private static String getNamespace() {
  //   // Fallback namespace is empty.
  //   return "";
  // }
  //
  // @Nullable
  // private static String getVersion() {
  //   // Specification lacks a way to specify non-fallback right now.
  //   return null;
  // }

  private static String getServiceName() {
    return System.getenv().getOrDefault("OTEL_SERVICE_NAME", "unknown_service:java");
  }

  @Override
  public List<Entity> detectEntities() {
    return Collections.singletonList(
        Entity.builder()
            .setEntityType(ENTITY_TYPE)
            .setSchemaUrl(SCHEMA_URL)
            .withIdentifying(
                builder -> {
                  // Note: Identifying attributes MUST be provided together.
                  builder.put(SERVICE_NAME, getServiceName());
                  // Disabled as breaking integration tests.
                  // builder.put(SERVICE_NAMESPACE, getNamespace());
                })
            // Disabled as breaking integration tests.
            // .withDescriptive(
            //     builder -> {
            //       if (!StringUtils.isNullOrEmpty(getVersion())) {
            //         builder.put(SERVICE_VERSION, getVersion());
            //       }
            //     })
            .build());
  }

  private ServiceDetector() {}

  public static final EntityDetector INSTANCE = new ServiceDetector();
}
