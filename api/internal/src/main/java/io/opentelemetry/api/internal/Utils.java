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
   * Throws an {@link IllegalArgumentException} if the argument is false. This method is similar to
   * {@code Preconditions.checkArgument(boolean, Object)} from Guava.
   *
   * @param expression a boolean expression
   * @param errorMessageTemplate a template for the exception message should the check fail. The
   *     message is formed by replacing each {@code %s} placeholder in the template with an
   *     argument. These are matched by position - the first {@code %s} gets {@code
   *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to the formatted message in
   *     square braces. Unmatched placeholders will be left as-is.
   * @param errorMessageArgs the arguments to be substituted into the message template. Arguments
   *     are converted to strings using {@link String#valueOf(Object)}.
   * @throws IllegalArgumentException if {@code expression} is false
   * @throws NullPointerException if the check fails and either {@code errorMessageTemplate} or
   *     {@code errorMessageArgs} is null (don't let this happen)
   */
  public static void checkArgument(
      boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  /**
   * Substitutes each {@code %s} in {@code template} with an argument. These are matched by
   * position: the first {@code %s} gets {@code args[0]}, etc. If there are more arguments than
   * placeholders, the unmatched arguments will be appended to the end of the formatted message in
   * square braces.
   *
   * <p>Copied from {@code Preconditions.format(String, Object...)} from Guava
   *
   * @param template a non-null string containing 0 or more {@code %s} placeholders.
   * @param args the arguments to be substituted into the message template. Arguments are converted
   *     to strings using {@link String#valueOf(Object)}. Arguments can be null.
   */
  // Note that this is somewhat-improperly used from Verify.java as well.
  private static String format(String template, Object... args) {
    // If no arguments return the template.
    if (args.length == 0) {
      return template;
    }

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template, templateStart, placeholderStart);
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template, templateStart, template.length());

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
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
