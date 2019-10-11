/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib.http.servlet;

/** Path matcher implementation for Ant-style path patterns. */
public class AntPathMatcher {

  /** Default path separator: '/'. */
  public static final char DEFAULT_PATH_SEPARATOR = '/';
  /** Default ignore case: false. */
  public static final boolean DEFAULT_IGNORE_CASE = false;

  private static final char ASTERISK = '*';
  private static final char QUESTION = '?';
  private static final int ASCII_CASE_DIFFERENCE_VALUE = 32;

  private final char pathSeparator;
  private final boolean ignoreCase;

  public AntPathMatcher() {
    this(DEFAULT_PATH_SEPARATOR, DEFAULT_IGNORE_CASE);
  }

  public AntPathMatcher(char pathSeparator, boolean ignoreCase) {
    this.pathSeparator = pathSeparator;
    this.ignoreCase = ignoreCase;
  }

  public boolean isMatch(String pattern, String path) {
    return doMatch(pattern.toCharArray(), 0, path.toCharArray(), 0);
  }

  private boolean doMatch(char[] pattern, int patternPointer, char[] path, int pathPointer) {
    if (isEmpty(pattern, patternPointer)) {
      return isEmpty(path, pathPointer);
    } else if (isEmpty(path, pathPointer) && pattern[patternPointer] == pathSeparator) {
      if (isEqualLength(pattern, 2, patternPointer) && pattern[patternPointer + 1] == ASTERISK) {
        return false;
      } else {
        return doMatch(pattern, patternPointer + 1, path, pathPointer);
      }
    }

    char patternStart = pattern[patternPointer];
    if (patternStart == ASTERISK) {

      if (isEqualLength(pattern, 1, patternPointer)) {
        return isEmpty(path, pathPointer)
            || ((path[pathPointer] != pathSeparator)
                && doMatch(pattern, patternPointer, path, pathPointer + 1));
      } else if (isDoubleAsteriskMatch(pattern, patternPointer, path, pathPointer)) {
        return true;
      }

      int start = pathPointer;
      while (start < path.length) {
        if (doMatch(pattern, patternPointer + 1, path, start)) {
          return true;
        }
        start++;
      }

      return doMatch(pattern, patternPointer + 1, path, start);
    }

    return (pathPointer < path.length)
        && (isEqual(path[pathPointer], patternStart) || patternStart == QUESTION)
        && doMatch(pattern, patternPointer + 1, path, pathPointer + 1);
  }

  private boolean isDoubleAsteriskMatch(
      char[] pattern, int patternPointer, char[] path, int pathPointer) {
    if (pattern[patternPointer + 1] != ASTERISK) {
      return false;
    } else if (pattern.length - patternPointer > 2) {
      return doMatch(pattern, patternPointer + 3, path, pathPointer);
    } else {
      return false;
    }
  }

  private boolean isEqual(char pathChar, char patternChar) {
    if (ignoreCase) {
      return pathChar == patternChar
          || ((pathChar > patternChar)
              ? pathChar == patternChar + ASCII_CASE_DIFFERENCE_VALUE
              : pathChar == patternChar - ASCII_CASE_DIFFERENCE_VALUE);
    } else {
      return pathChar == patternChar;
    }
  }

  private static boolean isEmpty(char[] characters, int pointer) {
    return characters.length == pointer;
  }

  private static boolean isEqualLength(char[] characters, int length, int pointer) {
    return characters.length - pointer == length;
  }
}
