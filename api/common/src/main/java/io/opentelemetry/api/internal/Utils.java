/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import javax.annotation.concurrent.Immutable;

/** General internal utility methods. */
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
}
