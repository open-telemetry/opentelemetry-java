/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.internal;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides a configured {@link ObjectMapper} for YAML declarative configuration parsing.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class YamlObjectMapper {

  private static final ObjectMapper INSTANCE;

  static {
    INSTANCE =
        new ObjectMapper()
            // Create empty object instances for keys which are present but have null values
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
    // Boxed primitives which are present but have null values should be set to null, rather than
    // empty instances
    INSTANCE.configOverride(String.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    INSTANCE.configOverride(Integer.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    INSTANCE.configOverride(Double.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    INSTANCE.configOverride(Boolean.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
  }

  private YamlObjectMapper() {}

  /**
   * Returns the configured {@link ObjectMapper} instance for parsing YAML declarative
   * configuration.
   *
   * @return the configured ObjectMapper
   */
  public static ObjectMapper getInstance() {
    return INSTANCE;
  }
}
