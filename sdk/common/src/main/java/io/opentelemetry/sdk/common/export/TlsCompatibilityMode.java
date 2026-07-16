/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/**
 * Controls the range of TLS protocol versions a {@link HttpSender} is willing to negotiate when
 * connecting to an HTTPS endpoint.
 *
 * <p>This is intentionally implementation-neutral: it says nothing about OkHttp's {@code
 * ConnectionSpec} or any other sender-specific concept. Each {@link HttpSenderProvider} maps it to
 * whatever mechanism its underlying HTTP client exposes.
 */
public enum TlsCompatibilityMode {
  /**
   * The default TLS configuration intended for modern servers and clients. In OkHttp terms, this
   * corresponds to {@code ConnectionSpec.MODERN_TLS}.
   */
  MODERN,

  /**
   * A broader set of TLS protocol versions for compatibility with older endpoints. For OkHttp
   * senders, this corresponds to {@code ConnectionSpec.COMPATIBLE_TLS}.
   *
   * <p>Prefer upgrading the client or server platform instead of using this mode when possible: it
   * widens the negotiable protocol range down to TLSv1.0.
   */
  COMPATIBLE
}
