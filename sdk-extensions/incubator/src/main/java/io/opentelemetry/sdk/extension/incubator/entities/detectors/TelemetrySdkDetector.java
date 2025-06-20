/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities.detectors;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import io.opentelemetry.sdk.extension.incubator.entities.Entity;
import io.opentelemetry.sdk.extension.incubator.entities.EntityDetector;
import java.util.Collection;
import java.util.Collections;

/**
 * Detection for {@code telemetry.sdk} entity.
 *
 * <p>See: <a
 * href="https://opentelemetry.io/docs/specs/semconv/resource/#telemetry-sdk">teleemtry.sdk
 * entity</a>
 */
public class TelemetrySdkDetector implements EntityDetector {
  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.28.0";
  private static final String ENTITY_TYPE = "telemetry.sdk";
  private static final AttributeKey<String> TELEMETRY_SDK_LANGUAGE =
      AttributeKey.stringKey("telemetry.sdk.language");
  private static final AttributeKey<String> TELEMETRY_SDK_NAME =
      AttributeKey.stringKey("telemetry.sdk.name");
  private static final AttributeKey<String> TELEMETRY_SDK_VERSION =
      AttributeKey.stringKey("telemetry.sdk.version");
  private static final Entity TELEMETRY_SDK;

  static {
    TELEMETRY_SDK =
        Entity.builder(ENTITY_TYPE)
            .setSchemaUrl(SCHEMA_URL)
            .withId(
                id -> {
                  id.put(TELEMETRY_SDK_NAME, "opentelemetry").put(TELEMETRY_SDK_LANGUAGE, "java");
                })
            .withDescription(desc -> desc.put(TELEMETRY_SDK_VERSION, OtelVersion.VERSION))
            .build();
  }

  @Override
  public Collection<Entity> detect() {
    return Collections.singletonList(TELEMETRY_SDK);
  }
}
