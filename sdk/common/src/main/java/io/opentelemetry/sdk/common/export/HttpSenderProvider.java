/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/**
 * A service provider interface (SPI) for providing {@link HttpSender}s backed by different HTTP
 * client libraries.
 *
 * @since 1.59.0
 */
public interface HttpSenderProvider {

  /** Returns a {@link HttpSender} configured with the provided config. */
  HttpSender createSender(HttpSenderConfig httpSenderConfig);
}
