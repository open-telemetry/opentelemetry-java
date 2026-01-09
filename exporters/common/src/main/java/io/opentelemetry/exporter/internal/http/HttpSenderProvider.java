/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

/**
 * A service provider interface (SPI) for providing {@link HttpSender}s backed by different HTTP
 * client libraries.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface HttpSenderProvider {

  /** Returns a {@link HttpSender} configured with the provided config. */
  HttpSender createSender(HttpSenderConfig httpSenderConfig);
}
