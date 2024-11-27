/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.detectors;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import io.opentelemetry.sdk.resources.Entity;
import io.opentelemetry.sdk.resources.EntityDetector;
import java.util.Collections;
import java.util.List;

/** Detector which finds the `telemetry.sdk` entity. */
public final class TelemetrySdkDetector implements EntityDetector {
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
            .withIdentifying(
                attributes -> {
                  attributes
                      .put(TELEMETRY_SDK_NAME, "opentelemetry")
                      .put(TELEMETRY_SDK_LANGUAGE, "java")
                      .put(TELEMETRY_SDK_VERSION, OtelVersion.VERSION);
                })
            .build();
  }

  @Override
  public List<Entity> detectEntities() {
    return Collections.singletonList(TELEMETRY_SDK);
  }

  private TelemetrySdkDetector() {}

  public static final TelemetrySdkDetector INSTANCE = new TelemetrySdkDetector();
}
