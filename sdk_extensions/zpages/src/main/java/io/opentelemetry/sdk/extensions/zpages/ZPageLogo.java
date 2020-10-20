/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.zpages;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ZPageLogo {
  private static final Logger logger = Logger.getLogger(ZPageLogo.class.getName());

  private ZPageLogo() {}

  /**
   * Get OpenTelemetry logo in base64 encoding.
   *
   * @return OpenTelemetry logo in base64 encoding.
   */
  public static String getLogoBase64() {
    try {
      InputStream in = ZPageLogo.class.getClassLoader().getResourceAsStream("logo.png");
      byte[] bytes = ByteStreams.toByteArray(in);
      return BaseEncoding.base64().encode(bytes);
    } catch (Throwable t) {
      logger.log(Level.WARNING, "error while getting OpenTelemetry Logo", t);
      return "";
    }
  }

  /**
   * Get OpenTelemetry favicon in base64 encoding.
   *
   * @return OpenTelemetry favicon in base64 encoding.
   */
  public static String getFaviconBase64() {
    try {

      InputStream in = ZPageLogo.class.getClassLoader().getResourceAsStream("favicon.png");
      byte[] bytes = ByteStreams.toByteArray(in);
      return BaseEncoding.base64().encode(bytes);
    } catch (Throwable t) {
      logger.log(Level.WARNING, "error while getting OpenTelemetry Logo", t);
      return "";
    }
  }
}
