/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Re-usable string predicates. */
public final class StringPredicates {
  private StringPredicates() {}

  /** A string predicaate that matches all strings. */
  public static final Predicate<String> ALL = value -> true;

  /** A string predicate that does exact string matching. */
  public static Predicate<String> exact(String match) {
    return input -> (input != null) && match.equals(input);
  }

  /** A string predicate that matches against a regular expression. */
  public static Predicate<String> regex(String regex) {
    return regex(Pattern.compile(regex));
  }

  /** A string predicate that matches against a regular expression. */
  public static Predicate<String> regex(Pattern pattern) {
    return input -> (input != null) && pattern.matcher(input).matches();
  }
}
