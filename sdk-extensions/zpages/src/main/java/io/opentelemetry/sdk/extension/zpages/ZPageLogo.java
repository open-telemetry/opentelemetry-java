/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
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
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      try (InputStream is = ZPageLogo.class.getClassLoader().getResourceAsStream("logo.png")) {
        readTo(is, os);
      }
    } catch (Throwable t) {
      logger.log(Level.WARNING, "error while getting OpenTelemetry Logo", t);
      return "";
    }
    byte[] bytes = os.toByteArray();
    return Base64.getEncoder().encodeToString(bytes);
  }

  /**
   * Get OpenTelemetry favicon in base64 encoding.
   *
   * @return OpenTelemetry favicon in base64 encoding.
   */
  public static String getFaviconBase64() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (InputStream is = ZPageLogo.class.getClassLoader().getResourceAsStream("favicon.png")) {
      readTo(is, os);
    } catch (Throwable t) {
      logger.log(Level.WARNING, "error while getting OpenTelemetry Logo", t);
      return "";
    }
    byte[] bytes = os.toByteArray();
    return Base64.getEncoder().encodeToString(bytes);
  }

  private static void readTo(InputStream is, ByteArrayOutputStream os) throws IOException {
    int b;
    while ((b = is.read()) != -1) {
      os.write(b);
    }
  }
}
