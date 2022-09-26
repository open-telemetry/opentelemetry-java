/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.sdk.resources.Resource;
import java.util.Properties;
import java.util.function.BiConsumer;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class OtlpUserAgent {

  private static final String userAgent;

  static {
    userAgent = "OTel OTLP Exporter Java/" + readVersion();
  }

  private static String readVersion() {
    Properties properties = new Properties();
    try {
      properties.load(
          Resource.class.getResourceAsStream(
              "/io/opentelemetry/exporter/internal/otlp/version.properties"));
    } catch (Exception e) {
      // we left the attribute empty
      return "unknown";
    }
    return properties.getProperty("sdk.version", "unknown");
  }

  /**
   * Return an OTLP {@code User-Agent} header value of the form {@code "OTel OTLP Exporter
   * Java/{version}"}.
   *
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#user-agent">OTLP
   *     Exporter User Agent</a>
   */
  public static String getUserAgent() {
    return userAgent;
  }

  /**
   * Call the {@code consumer with} an OTLP {@code User-Agent} header value of the form {@code "OTel
   * OTLP Exporter Java/{version}"}.
   *
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#user-agent">OTLP
   *     Exporter User Agent</a>
   */
  public static void addUserAgentHeader(BiConsumer<String, String> consumer) {
    consumer.accept("User-Agent", userAgent);
  }

  private OtlpUserAgent() {}
}
