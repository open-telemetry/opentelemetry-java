/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import java.util.function.Predicate;
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
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        return new GlobPatternPredicate(globPattern, globPattern.codePoints().toArray());
      }
    }
    return new GlobPatternPredicate(globPattern, null);
  }

  /**
   * A predicate which evaluates if a test string matches the {@link #globPattern}, and which has a
   * valid {@link #toString()} implementation.
   */
  private static class GlobPatternPredicate implements Predicate<String> {
    private final String globPattern;
    @Nullable private final int[] pattern;

    private GlobPatternPredicate(String globPattern, @Nullable int[] pattern) {
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
        return matches(pattern, s);
      }
      // Exact match
      return globPattern.equals(s);
    }

    @Override
    public String toString() {
      return "GlobPatternPredicate{globPattern=" + globPattern + "}";
    }

    /**
     * Retries only the most recent star, bounding matching time to O(pattern length * input length)
     * with constant per-match space.
     */
    private static boolean matches(int[] pattern, String s) {
      int patternIndex = 0;
      int inputIndex = 0;
      int starIndex = -1;
      int starInputIndex = -1;
      while (inputIndex < s.length()) {
        if (patternIndex < pattern.length && pattern[patternIndex] == '*') {
          // Try an empty match first, remembering this star as the next retry point.
          starIndex = patternIndex++;
          starInputIndex = inputIndex;
          continue;
        }
        // Match whole code points so '?' consumes a supplementary character as one unit.
        int codePoint = s.codePointAt(inputIndex);
        if (patternIndex < pattern.length
            && (pattern[patternIndex] == codePoint
                || (pattern[patternIndex] == '?' && matchesWildcard(codePoint)))) {
          patternIndex++;
          inputIndex += Character.charCount(codePoint);
          continue;
        }
        if (starIndex == -1) {
          return false;
        }
        // On mismatch, extend the most recent star by one code point and retry its suffix.
        int starCodePoint = s.codePointAt(starInputIndex);
        if (!matchesWildcard(starCodePoint)) {
          return false;
        }
        starInputIndex += Character.charCount(starCodePoint);
        inputIndex = starInputIndex;
        patternIndex = starIndex + 1;
      }
      // With the input exhausted, only stars can match the remaining pattern (as empty matches).
      while (patternIndex < pattern.length && pattern[patternIndex] == '*') {
        patternIndex++;
      }
      return patternIndex == pattern.length;
    }

    private static boolean matchesWildcard(int codePoint) {
      // Preserve the line terminator exclusions of regex '.' without DOTALL.
      return codePoint != '\n'
          && codePoint != '\r'
          && codePoint != 0x85
          && codePoint != 0x2028
          && codePoint != 0x2029;
    }
  }
}
