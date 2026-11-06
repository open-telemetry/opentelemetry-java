/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import java.io.InputStream;
import java.util.Map;

/**
 * Parses a declarative configuration YAML stream into generic Java objects.
 *
 * <p>Implementations are discovered with {@link java.util.ServiceLoader}. If exactly one
 * implementation is available, it is used. If multiple implementations are available, set a system
 * property or environment variable whose name is this interface's fully qualified class name and
 * whose value is the fully qualified class name of the desired implementation. If no implementation
 * is selected, the first discovered implementation is used and a warning is logged. The built-in
 * parser is used if no implementation is registered.
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
   */
  Object parse(
      InputStream configuration,
      Map<String, String> environmentVariables,
      Map<Object, Object> systemProperties);
}
