/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities.detectors;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.entities.EntityProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import io.opentelemetry.sdk.extension.incubator.entities.ResourceDetector;

/**
 * Detection for {@code telemetry.sdk} entity.
 *
 * <p>See: <a
 * href="https://opentelemetry.io/docs/specs/semconv/resource/#telemetry-sdk">teleemtry.sdk
 * entity</a>
 */
public final class TelemetrySdkDetector implements ResourceDetector {
  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.34.0";
  private static final String ENTITY_TYPE = "telemetry.sdk";
  private static final AttributeKey<String> TELEMETRY_SDK_LANGUAGE =
      AttributeKey.stringKey("telemetry.sdk.language");
  private static final AttributeKey<String> TELEMETRY_SDK_NAME =
      AttributeKey.stringKey("telemetry.sdk.name");
  private static final AttributeKey<String> TELEMETRY_SDK_VERSION =
      AttributeKey.stringKey("telemetry.sdk.version");

  @Override
  public CompletableResultCode report(EntityProvider provider) {
    provider
        .attachOrUpdateEntity(ENTITY_TYPE)
        .setSchemaUrl(SCHEMA_URL)
        .withId(
            Attributes.builder()
                .put(TELEMETRY_SDK_NAME, "opentelemetry")
                .put(TELEMETRY_SDK_LANGUAGE, "java")
                .build())
        .withDescription(
            Attributes.builder().put(TELEMETRY_SDK_VERSION, OtelVersion.VERSION).build())
        .emit();
    return CompletableResultCode.ofSuccess();
  }
}
