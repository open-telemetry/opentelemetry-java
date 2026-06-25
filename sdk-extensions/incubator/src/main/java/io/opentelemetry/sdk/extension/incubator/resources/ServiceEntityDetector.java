/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.internal.Entity;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/** Detects `service` and `service.instance` entities. */
public class ServiceEntityDetector implements EntityDetector {
  // TODO - Pull this in from semconv.
  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.40.0";
  private static final String SERVICE_TYPE = "service";
  private static final String SERVICE_INSTANCE_TYPE = "service.instance";

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
  public static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  // multiple calls to this detector provider should return the same value
  private static final String RANDOM = UUID.randomUUID().toString();

  @Override
  public String getName() {
    return "service";
  }

  @Override
  public Collection<Entity> detect(ConfigProperties config) {
    String serviceName = config.getString("otel.service.name");

    return Arrays.asList(
        Entity.builder(SERVICE_TYPE)
            .setId(Attributes.builder().put(SERVICE_NAME, serviceName).build())
            // TODO: Add other service descriptive attributes.
            .setSchemaUrl(SCHEMA_URL)
            .build(),
        Entity.builder(SERVICE_INSTANCE_TYPE)
            .setId(
                Attributes.builder()
                    // TODO: pull from env variable if needed.
                    .put(SERVICE_INSTANCE_ID, RANDOM)
                    .build())
            .setSchemaUrl(SCHEMA_URL)
            .build());
  }
}
