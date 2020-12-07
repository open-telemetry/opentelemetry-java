/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.util.Objects;
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

  /**
   * Validates that the array of Strings is 1) even in length, and 2) they can be formed into valid
   * pairs where the first item in the pair is not null.
   *
   * <p>TODO: write unit tests for this method.
   *
   * @param keyValuePairs The String[] to validate for correctness.
   * @throws IllegalArgumentException if any of the preconditions are violated.
   */
  public static void validateLabelPairs(String[] keyValuePairs) {
    checkArgument(
        keyValuePairs.length % 2 == 0,
        "You must provide an even number of key/value pair arguments.");
    for (int i = 0; i < keyValuePairs.length; i += 2) {
      String key = keyValuePairs[i];
      Objects.requireNonNull(key, "You cannot provide null keys for label creation.");
    }
  }
}
