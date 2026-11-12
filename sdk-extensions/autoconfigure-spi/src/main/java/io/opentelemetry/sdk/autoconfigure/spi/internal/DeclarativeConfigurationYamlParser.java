/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import java.io.InputStream;
import java.util.Map;

/**
 * Parses a declarative configuration YAML stream into generic Java objects.
 *
 * <p>Implementations must apply environment variable and system property substitution according to
 * the OpenTelemetry Configuration Data Model specification. Substitution is performed on scalar
 * values, not mapping keys. For unquoted scalar values, substituted text must be parsed using the
 * same YAML scalar rules as the original text.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface DeclarativeConfigurationYamlParser {

  /**
   * Parse a YAML stream.
   *
   * @param configuration the YAML input stream
   * @param environmentVariables environment variables used for substitution
   * @param systemProperties system properties used for substitution
   * @return the parsed configuration
   * @throws DeclarativeConfigException if the YAML cannot be parsed
   */
  Object parse(
      InputStream configuration,
      Map<String, String> environmentVariables,
      Map<Object, Object> systemProperties);
}
