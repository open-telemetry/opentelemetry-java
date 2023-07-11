/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpentelemetryConfiguration;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;

class ConfigurationReader {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Yaml YAML = new Yaml();

  private ConfigurationReader() {}

  /** Parse the {@code configuration} YAML and return the {@link OpentelemetryConfiguration}. */
  static OpentelemetryConfiguration parse(InputStream configuration) {
    Object yamlObj = YAML.load(configuration);
    return MAPPER.convertValue(yamlObj, OpentelemetryConfiguration.class);
  }
}
