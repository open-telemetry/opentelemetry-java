/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Creates an OpenTelemetry {@link Resource} from environment configuration.
 *
 * <p>This class is intentionally self-contained (no dependencies on other autoconfigure-internal
 * classes). Do not add dependencies on non-API, non-SPI classes.
 */
final class EnvironmentResource {

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

  static final String ATTRIBUTE_PROPERTY = "otel.resource.attributes";
  static final String SERVICE_NAME_PROPERTY = "otel.service.name";

  @SuppressWarnings("JdkObsolete") // Recommended alternative was introduced in java 10
  static Resource createEnvironmentResource(ConfigProperties config) {
    AttributesBuilder resourceAttributes = Attributes.builder();
    for (Map.Entry<String, String> entry : config.getMap(ATTRIBUTE_PROPERTY).entrySet()) {
      resourceAttributes.put(
          entry.getKey(),
          // Attributes specified via otel.resource.attributes follow the W3C Baggage spec and
          // characters outside the baggage-octet range are percent encoded
          // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md#specifying-resource-information-via-an-environment-variable
          decodeResourceAttributes(entry.getValue()));
    }
    String serviceName = config.getString(SERVICE_NAME_PROPERTY);
    if (serviceName != null) {
      resourceAttributes.put(SERVICE_NAME, serviceName);
    }

    return Resource.create(resourceAttributes.build());
  }

  /**
   * Decodes percent-encoded characters in resource attribute values per W3C Baggage spec.
   *
   * <p>Unlike {@link java.net.URLDecoder}, this method:
   *
   * <ul>
   *   <li>Preserves '+' as a literal plus sign (URLDecoder decodes '+' as space)
   *   <li>Preserves invalid percent sequences as literals (e.g., "%2G", "%", "%2")
   *   <li>Supports multi-byte UTF-8 sequences (e.g., "%C3%A9" decodes to "é")
   * </ul>
   *
   * @param value the percent-encoded string
   * @return the decoded string
   */
  private static String decodeResourceAttributes(String value) {
    // no percent signs means nothing to decode
    if (value.indexOf('%') < 0) {
      return value;
    }

    int n = value.length();
    // Use byte array to properly handle multi-byte UTF-8 sequences
    byte[] bytes = new byte[n];
    int pos = 0;

    for (int i = 0; i < n; i++) {
      char c = value.charAt(i);
      // Check for percent-encoded sequence i.e. '%' followed by two hex digits
      if (c == '%' && i + 2 < n) {
        int d1 = Character.digit(value.charAt(i + 1), 16);
        int d2 = Character.digit(value.charAt(i + 2), 16);
        // Valid hex digits return 0-15, invalid returns -1
        if (d1 != -1 && d2 != -1) {
          // Combine two hex digits into a single byte (e.g., "2F" becomes 0x2F)
          bytes[pos++] = (byte) ((d1 << 4) + d2);
          // Skip the two hex digits (loop will also do i++)
          i += 2;
          continue;
        }
      }
      // Keep '+' as '+' (unlike URLDecoder) and preserve invalid percent sequences which will be
      // treated as literals
      bytes[pos++] = (byte) c;
    }
    return new String(bytes, 0, pos, StandardCharsets.UTF_8);
  }

  private EnvironmentResource() {}
}
