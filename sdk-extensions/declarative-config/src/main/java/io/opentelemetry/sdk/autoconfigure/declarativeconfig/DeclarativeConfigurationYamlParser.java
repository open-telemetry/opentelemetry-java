/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.io.InputStream;
import java.util.Map;

/**
 * Parses a declarative configuration YAML stream into generic Java objects.
 *
 * <p>Implementations are discovered with {@link java.util.ServiceLoader}. The implementation with
 * the highest {@link #order()} is used. The built-in parser has the lowest possible order and is
 * used when no other implementation is registered.
 *
 * <p>Implementations must apply environment variable and system property substitution according to
 * the OpenTelemetry Configuration Data Model specification. Substitution is performed on scalar
 * values, not mapping keys. For unquoted scalar values, substituted text must be parsed using the
 * same YAML scalar rules as the original text.
 */
public interface DeclarativeConfigurationYamlParser extends Ordered {

  /**
   * Parse a YAML stream.
   *
   * @param configuration the YAML input stream
   * @param environmentVariables environment variables used for substitution
   * @param systemProperties system properties used for substitution
   * @return the parsed configuration
   */
  Object parse(
      InputStream configuration,
      Map<String, String> environmentVariables,
      Map<Object, Object> systemProperties);
}
