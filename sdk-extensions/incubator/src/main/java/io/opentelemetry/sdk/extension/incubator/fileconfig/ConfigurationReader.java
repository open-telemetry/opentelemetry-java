/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpentelemetryConfiguration;
import java.io.IOException;
import java.io.InputStream;

class ConfigurationReader {

  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

  private ConfigurationReader() {}

  /**
   * Parse the {@code configuration} YAML and return the {@link OpentelemetryConfiguration}.
   *
   * @throws IOException if unable to parse
   */
  static OpentelemetryConfiguration parse(InputStream configuration) throws IOException {
    return MAPPER.readValue(configuration, OpentelemetryConfiguration.class);
  }
}
