/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import java.util.Collection;
import java.util.Collections;

/**
 * Detection for {@code telemetry.sdk} entity.
 *
 * <p>See: <a href=
 * "https://opentelemetry.io/docs/specs/semconv/resource/#telemetry-sdk">teleemtry.sdk entity</a>
 */
public final class TelemetrySdkEntityDetector implements EntityDetector {
  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.40.0";
  private static final String ENTITY_TYPE = "telemetry.sdk";
  private static final AttributeKey<String> TELEMETRY_SDK_LANGUAGE =
      AttributeKey.stringKey("telemetry.sdk.language");
  private static final AttributeKey<String> TELEMETRY_SDK_NAME =
      AttributeKey.stringKey("telemetry.sdk.name");
  private static final AttributeKey<String> TELEMETRY_SDK_VERSION =
      AttributeKey.stringKey("telemetry.sdk.version");

  @Override
  public String getName() {
    return "telemetry.sdk";
  }

  @Override
  public Collection<Entity> detect(ConfigProperties config) {
    return Collections.singletonList(
        Entity.builder(ENTITY_TYPE)
            .setSchemaUrl(SCHEMA_URL)
            .setIdentity(
                Attributes.builder()
                    .put(TELEMETRY_SDK_NAME, "opentelemetry")
                    .put(TELEMETRY_SDK_LANGUAGE, "java")
                    .build())
            .setDescription(
                Attributes.builder().put(TELEMETRY_SDK_VERSION, OtelVersion.VERSION).build())
            .build());
  }
}
