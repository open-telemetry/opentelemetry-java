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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for {@link AntPathRequestMatcher}. */
public class AntPathRequestMatcherTest {

  @Test
  public void shouldMatchAllIfNoIncludesOrExcludesProvided() {
    AntPathRequestMatcher matcher = new AntPathRequestMatcher(null, null);
    assertTrue(matcher.isMatch("/"));
    assertTrue(matcher.isMatch("/actuator/health"));
  }

  @Test
  public void shouldMatchAllExceptProvidedExcludes() {
    AntPathRequestMatcher matcher = new AntPathRequestMatcher("/**", "/actuator/**,/static/**");
    assertTrue(matcher.isMatch("/"));
    assertTrue(matcher.isMatch("/api/states/TX"));
    assertTrue(matcher.isMatch("/pages/home.jsp"));
    assertFalse(matcher.isMatch("/actuator"));
    assertFalse(matcher.isMatch("/actuator/health"));
    assertFalse(matcher.isMatch("/static/css/style.css"));
  }

  @Test
  public void shouldMatchOnlyProvidedIncludes() {
    AntPathRequestMatcher matcher = new AntPathRequestMatcher("/api/**,/uploads/**", "");
    assertFalse(matcher.isMatch("/"));
    assertTrue(matcher.isMatch("/api/stations/DAL"));
    assertFalse(matcher.isMatch("/pages/home.jsp"));
    assertFalse(matcher.isMatch("/actuator"));
    assertFalse(matcher.isMatch("/actuator/health"));
    assertFalse(matcher.isMatch("/static/css/style.css"));
  }
}
