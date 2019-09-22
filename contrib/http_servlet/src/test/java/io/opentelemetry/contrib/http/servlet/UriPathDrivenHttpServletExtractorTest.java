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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;

/** Unit tests for {@link UriPathDrivenHttpServletExtractor}. */
public class UriPathDrivenHttpServletExtractorTest {

  @Test
  public void shouldExtractRequestValuesWhenNoQueryParameters() {
    String method = "POST";
    String url = "https://api.example.com/users";
    String path = "/users";
    int status = HttpServletResponse.SC_CREATED;
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn(method);
    when(request.getRequestURL()).thenReturn(new StringBuffer(url));
    when(request.getRequestURI()).thenReturn(path);
    when(response.getStatus()).thenReturn(status);
    UriPathDrivenHttpServletExtractor extractor = new UriPathDrivenHttpServletExtractor();
    assertEquals(method, extractor.getMethod(request));
    assertEquals(url, extractor.getUrl(request));
    assertEquals(path, extractor.getRoute(request));
    assertEquals(status, extractor.getStatusCode(response));
  }

  @Test
  public void shouldExtractRequestValuesWhenQueryParameters() {
    String method = "GET";
    String baseUrl = "https://api.example.com/users";
    String queryParams = "q=email:*example.org&limit=10&offset=1";
    String url = baseUrl + "?" + queryParams;
    String path = "/users";
    int status = HttpServletResponse.SC_OK;
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn(method);
    when(request.getRequestURL()).thenReturn(new StringBuffer(baseUrl));
    when(request.getQueryString()).thenReturn(queryParams);
    when(request.getRequestURI()).thenReturn(path);
    when(response.getStatus()).thenReturn(status);
    UriPathDrivenHttpServletExtractor extractor = new UriPathDrivenHttpServletExtractor();
    assertEquals(method, extractor.getMethod(request));
    assertEquals(url, extractor.getUrl(request));
    assertEquals(path, extractor.getRoute(request));
    assertEquals(status, extractor.getStatusCode(response));
  }

  @Test
  public void shouldReturnZeroIfResponseIsNull() {
    UriPathDrivenHttpServletExtractor extractor = new UriPathDrivenHttpServletExtractor();
    assertEquals(0, extractor.getStatusCode(null));
  }
}
