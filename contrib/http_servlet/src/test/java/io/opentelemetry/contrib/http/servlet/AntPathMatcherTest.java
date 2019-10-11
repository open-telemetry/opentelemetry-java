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

/** Unit tests for {@link AntPathMatcher}. */
public class AntPathMatcherTest {

  @Test
  public void shouldMatchPatternsWithNoWildcards() {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    assertTrue(pathMatcher.isMatch("users", "users"));
    assertTrue(pathMatcher.isMatch("/users", "/users"));
    assertTrue(pathMatcher.isMatch("https://example.org", "https://example.org"));
    assertTrue(pathMatcher.isMatch("/sm-logo.jpg", "/sm-logo.jpg"));
    assertFalse(pathMatcher.isMatch("/sm-logo.jpg", "sm-logo.jpg"));
    assertFalse(pathMatcher.isMatch("users", "/users"));
    assertFalse(pathMatcher.isMatch("/users", "users"));
    assertTrue(pathMatcher.isMatch("", ""));
  }

  @Test
  public void shouldMatchPatternsWithSingleCharacterWildcards() {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    assertTrue(pathMatcher.isMatch("us?rs", "users"));
    assertTrue(pathMatcher.isMatch("??ers", "users"));
    assertTrue(pathMatcher.isMatch("user?", "users"));
    assertTrue(pathMatcher.isMatch("use??", "users"));
    assertTrue(pathMatcher.isMatch("?ser?", "users"));
    assertFalse(pathMatcher.isMatch("use?", "users"));
    assertFalse(pathMatcher.isMatch("user?", "users/"));
    assertFalse(pathMatcher.isMatch("user?", "uesrs"));
    assertTrue(pathMatcher.isMatch("/?", "/a"));
    assertTrue(pathMatcher.isMatch("/?/a", "/a/a"));
    assertTrue(pathMatcher.isMatch("/a/?", "/a/b"));
    assertTrue(pathMatcher.isMatch("/??/a", "/aa/a"));
    assertTrue(pathMatcher.isMatch("/a/??", "/a/bb"));
    assertTrue(pathMatcher.isMatch("/?", "/a"));
  }

  @Test
  public void shouldMatchPatternsWithSinglePathElementWildcards() {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    assertTrue(pathMatcher.isMatch("*", "users"));
    assertTrue(pathMatcher.isMatch("users*", "users"));
    assertTrue(pathMatcher.isMatch("users*", "users-creation"));
    assertTrue(pathMatcher.isMatch("users/*", "users/89748374"));
    assertTrue(pathMatcher.isMatch("users/*", "users/"));
    assertTrue(pathMatcher.isMatch("*users*", "endusers-us"));
    assertTrue(pathMatcher.isMatch("*users", "endusers"));
    assertTrue(pathMatcher.isMatch("/*/*", "/places/"));
    assertTrue(pathMatcher.isMatch("/*/*", "/places/23987277374"));
    assertTrue(pathMatcher.isMatch("/*/*", "/places/DAL.us"));
    assertTrue(pathMatcher.isMatch("us-*-1", "us-east-1"));
    assertFalse(pathMatcher.isMatch("user*", "usr"));
    assertFalse(pathMatcher.isMatch("user*", "usruser"));
    assertFalse(pathMatcher.isMatch("place*", "place/"));
    assertFalse(pathMatcher.isMatch("place*", "place/KCKS"));
    assertFalse(pathMatcher.isMatch("/places/*", "/places"));
    assertFalse(pathMatcher.isMatch("*west*", "wstwst"));
    assertTrue(pathMatcher.isMatch("*west*", "eu-west-1"));
    assertFalse(pathMatcher.isMatch("*west", "tsttst"));
    assertFalse(pathMatcher.isMatch("*-*-1", "use1"));
  }

  @Test
  public void shouldMatchPatternsWithMulitPathElementWildcards() {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    assertTrue(pathMatcher.isMatch("/**", "/"));
    assertTrue(pathMatcher.isMatch("/**", "/places/0472972"));
    assertTrue(pathMatcher.isMatch("/*/**", "/places/US"));
    assertTrue(pathMatcher.isMatch("/*/**", "/places/US/KS"));
    assertTrue(pathMatcher.isMatch("/**/*", "/places/US/KS"));
    assertTrue(pathMatcher.isMatch("/places/**", "/places"));
    assertTrue(pathMatcher.isMatch("/places/**/Franklin", "/places/US/TN/Franklin"));
    assertTrue(pathMatcher.isMatch("/places/**/Franklin", "/places/US/TN/Franklin/Franklin"));
    assertTrue(pathMatcher.isMatch("/**/Washington", "/places/US/OH/Washington"));
    assertFalse(pathMatcher.isMatch("/**/Washington", "/places/US/OH/PortWashington"));
    assertFalse(pathMatcher.isMatch("/**/Washington", "/places/US/PA/Washingtonville"));
    assertTrue(pathMatcher.isMatch("/places/**/**/places", "/places/US/IA/Franklin/places"));
    assertTrue(pathMatcher.isMatch("/*place*/**/KS/**", "/places/US/KS/Hiawatha"));
    assertTrue(pathMatcher.isMatch("/*place*/**/KS/*", "/places/US/KS/Hiawatha"));
    assertTrue(pathMatcher.isMatch("/*place*/**/KS/*", "/places/cities/US/KS/Hiawatha"));
    assertTrue(pathMatcher.isMatch("/*place*/**/KS/**", "/places/US/KS/Brown/Hiawatha"));
    assertTrue(pathMatcher.isMatch("/*place*/**/KS/**", "/places/US/KS/Brown/Hiawatha/logo.jpg"));
  }

  @Test
  public void shouldMatchPatternsWhenIgnoreCaseSet() {
    AntPathMatcher pathMatcher = new AntPathMatcher('/', true);
    assertTrue(
        pathMatcher.isMatch(
            "https://api.codekaizen.org/**", "https://API.CodeKaizen.org/projects"));
  }

  @Test
  public void shouldMatchPatternsWhenCustomSeparator() {
    AntPathMatcher pathMatcher = new AntPathMatcher(':', true);
    assertTrue(pathMatcher.isMatch("urn:uuid:*", "urn:uuid:8C1D04F2-AD51-4A6A-870A-649ACBC10A60"));
    assertFalse(
        pathMatcher.isMatch(
            "urn:uuid:*",
            "urn:uuid:8C1D04F2-AD51-4A6A-870A-649ACBC10A60:0825598B-784B-4D7D-929C-C5FBF3B9397A"));
    assertTrue(
        pathMatcher.isMatch(
            "arn:aws:iam:123456789012:**", "arn:aws:iam:123456789012:user:kbrockhoff"));
  }
}
