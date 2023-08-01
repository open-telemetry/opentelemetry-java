/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.sdk.common.internal.OtelVersion;
import java.util.function.BiConsumer;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OtlpUserAgent {

  private static final String userAgent = "OTel-OTLP-Exporter-Java/" + OtelVersion.VERSION;

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
