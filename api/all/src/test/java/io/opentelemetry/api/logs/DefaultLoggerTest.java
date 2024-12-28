/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.testing.internal.AbstractDefaultLoggerTest;
import org.junit.jupiter.api.Disabled;

@Disabled
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
