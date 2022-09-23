/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.zpages;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class ZPageLogo {

  private static final String LOGO = "logo.png";
  private static final String FAVICON = "favicon.png";

  private ZPageLogo() {}

  static String getLogoPath() {
    return "/" + LOGO;
  }

  static String getFaviconPath() {
    return "/" + FAVICON;
  }

  static void registerStaticResources(HttpServer httpServer) {
    registerStaticResource(httpServer, getLogoPath(), getResourceBytes(LOGO));
    registerStaticResource(httpServer, getFaviconPath(), getResourceBytes(FAVICON));
  }

  private static void registerStaticResource(HttpServer httpServer, String path, byte[] bytes) {
    httpServer.createContext(
        path,
        exchange -> {
          try {
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
          } finally {
            exchange.close();
          }
        });
  }

  private static byte[] getResourceBytes(String resource) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      try (InputStream is = ZPageLogo.class.getClassLoader().getResourceAsStream(resource)) {
        readTo(is, os);
      }
    } catch (Throwable t) {
      throw new IllegalStateException("Error retrieving OpenTelemetry resource " + resource, t);
    }
    return os.toByteArray();
  }

  private static void readTo(InputStream is, ByteArrayOutputStream os) throws IOException {
    int b;
    while ((b = is.read()) != -1) {
      os.write(b);
    }
  }
}
