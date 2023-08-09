/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.InputStream;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

class ConfigurationReader {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ConfigurationReader() {}

  /** Parse the {@code configuration} YAML and return the {@link OpenTelemetryConfiguration}. */
  static OpenTelemetryConfiguration parse(InputStream configuration) {
    LoadSettings settings = LoadSettings.builder().build();
    Load yaml = new Load(settings);
    Object yamlObj = yaml.loadFromInputStream(configuration);
    return MAPPER.convertValue(yamlObj, OpenTelemetryConfiguration.class);
  }
}
