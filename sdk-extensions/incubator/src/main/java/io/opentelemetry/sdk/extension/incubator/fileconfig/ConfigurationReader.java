/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.InputStream;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

final class ConfigurationReader {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          // Create empty object instances for keys which are present but have null values
          .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

  private ConfigurationReader() {}

  /** Parse the {@code configuration} YAML and return the {@link OpenTelemetryConfiguration}. */
  static OpenTelemetryConfiguration parse(InputStream configuration) {
    LoadSettings settings = LoadSettings.builder().build();
    Load yaml = new Load(settings);
    Object yamlObj = yaml.loadFromInputStream(configuration);
    return MAPPER.convertValue(yamlObj, OpenTelemetryConfiguration.class);
  }
}
