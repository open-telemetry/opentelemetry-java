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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Splitter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides checking of URL paths to determine whether telemetry should be recorded for the path in
 * question.
 */
class AntPathRequestMatcher {

  private static final String ALL_MATCH = "/**";

  private final Set<String> includes = new LinkedHashSet<>();
  private final Set<String> excludes = new LinkedHashSet<>();
  private final AntPathMatcher internalMatcher;

  AntPathRequestMatcher(String includesCsv, String excludesCsv) {
    super();
    if (!isNullOrEmpty(includesCsv)) {
      for (String include : Splitter.on(',').split(includesCsv)) {
        includes.add(include);
      }
    }
    if (includes.isEmpty()) {
      includes.add(ALL_MATCH);
    }
    if (!isNullOrEmpty(excludesCsv)) {
      for (String exclude : Splitter.on(',').split(excludesCsv)) {
        excludes.add(exclude);
      }
    }
    internalMatcher = new AntPathMatcher();
  }

  public boolean isMatch(String uriPath) {
    boolean result = false;
    for (String include : includes) {
      result = internalMatcher.isMatch(include, uriPath);
      if (result) {
        break;
      }
    }
    if (result) {
      for (String exclude : excludes) {
        if (internalMatcher.isMatch(exclude, uriPath)) {
          result = false;
          break;
        }
      }
    }
    return result;
  }
}
