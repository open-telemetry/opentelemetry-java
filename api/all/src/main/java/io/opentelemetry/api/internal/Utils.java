/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * General internal utility methods.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public final class Utils {

  private Utils() {}

  /**
   * Throws an {@link IllegalArgumentException} if the argument is false. This method is similar to
   * {@code Preconditions.checkArgument(boolean, Object)} from Guava.
   *
   * @param isValid whether the argument check passed.
   * @param errorMessage the message to use for the exception.
   */
  public static void checkArgument(boolean isValid, String errorMessage) {
    if (!isValid) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  /**
   * Logs a warning message if the argument is false.
   *
   * @param logger the logger instance that writes message.
   * @param isValid whether the argument check passed.
   * @param warnMessage the message to use for the warning log.
   */
  public static void warnOnArgument(Logger logger, boolean isValid, String warnMessage) {
    if (!isValid) {
      logger.log(Level.WARNING, warnMessage);
    }
  }
}
