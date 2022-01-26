/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.internal.testing.slf4j;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Suppresses console output of the named {@link java.util.logging.Logger}. */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SuppressLogger.SuppressLoggers.class)
public @interface SuppressLogger {
  /** The class whose {@link java.util.logging.Logger} will be suppressed. */
  Class<?> value();

  @Retention(RetentionPolicy.RUNTIME)
  @interface SuppressLoggers {
    SuppressLogger[] value();
  }
}
