/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Utilities for glob pattern matching.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class GlobUtil {

  private GlobUtil() {}

  /**
   * Return a predicate that returns {@code true} if a string matches the {@code globPattern}.
   *
   * <p>{@code globPattern} may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  public static Predicate<String> createGlobPatternPredicate(String globPattern) {
    // If globPattern contains '*' or '?', convert it to a regex and return corresponding
    // predicate
    Pattern pattern = null;
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        pattern = toRegexPattern(globPattern);
        break;
      }
    }
    return new GlobPatternPredicate(globPattern, pattern);
  }

  /**
   * Transform the {@code globPattern} to a regex by converting {@code *} to {@code .*}, {@code ?}
   * to {@code .}, and escaping other regex special characters.
   */
  private static Pattern toRegexPattern(String globPattern) {
    int tokenStart = -1;
    StringBuilder patternBuilder = new StringBuilder();
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        if (tokenStart != -1) {
          patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart, i)));
          tokenStart = -1;
        }
        if (c == '*') {
          patternBuilder.append(".*");
        } else {
          // c == '?'
          patternBuilder.append(".");
        }
      } else {
        if (tokenStart == -1) {
          tokenStart = i;
        }
      }
    }
    if (tokenStart != -1) {
      patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart)));
    }
    return Pattern.compile(patternBuilder.toString());
  }

  /**
   * A predicate which evaluates if a test string matches the {@link #globPattern}, and which has a
   * valid {@link #toString()} implementation.
   */
  private static class GlobPatternPredicate implements Predicate<String> {
    private final String globPattern;
    @Nullable private final Pattern pattern;

    private GlobPatternPredicate(String globPattern, @Nullable Pattern pattern) {
      this.globPattern = globPattern;
      this.pattern = pattern;
    }

    @Override
    public boolean test(String s) {
      // Match all
      if (globPattern.equals("*")) {
        return true;
      }
      if (pattern != null) {
        return pattern.matcher(s).matches();
      }
      // Exact match, ignoring case
      return globPattern.equalsIgnoreCase(s);
    }

    @Override
    public String toString() {
      return "GlobPatternPredicate{globPattern=" + globPattern + "}";
    }
  }
}
