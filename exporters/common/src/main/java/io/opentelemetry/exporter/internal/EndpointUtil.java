/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utilities for validating exporter endpoints.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class EndpointUtil {

  /** Validate an exporter endpoint. */
  public static URI validateEndpoint(String endpoint) {
    URI uri;
    try {
      uri = new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid endpoint, must be a URL: " + endpoint, e);
    }

    if (uri.getScheme() == null
        || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
      throw new IllegalArgumentException(
          "Invalid endpoint, must start with http:// or https://: " + uri);
    }
    if (!hasHost(uri, endpoint)) {
      throw new IllegalArgumentException(
          "Invalid endpoint, must start with http:// or https://: " + uri);
    }
    return uri;
  }

  /**
   * {@link URI#getHost()} follows RFC 2396 and returns {@code null} for some valid DNS names
   * (JDK-8188305), for example a label that starts with a digit. {@link URL#getHost()} accepts
   * those names, matching {@code OtlpConfigUtil.validateEndpoint}.
   */
  private static boolean hasHost(URI uri, String endpoint) {
    if (uri.getHost() != null) {
      return true;
    }
    try {
      String host = new URL(endpoint).getHost();
      return host != null && !host.isEmpty();
    } catch (MalformedURLException e) {
      return false;
    }
  }

  private EndpointUtil() {}
}
