/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

class DefaultLoggerTest extends AbstractDefaultLoggerTest {

  @Override
  protected LoggerProvider getLoggerProvider() {
    return DefaultLoggerProvider.getInstance();
  }

  @Override
  protected Logger getLogger() {
    return DefaultLogger.getInstance();
  }
}
