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

import javax.annotation.Nullable;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Unit tests for {@link SpringMvcDrivenHttpExtractor}. */
public class SpringMvcDrivenHttpExtractorTest {

  @Nullable private WebApplicationContext applicationContext;
  private RequestMappingHandlerMapping requestHandlerMappings;

  @Before
  public void setUp() {
    MockServletContext sc = new MockServletContext("");
    sc.addInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "classpath:simple-web-context.xml");
    ServletContextListener listener = new ContextLoaderListener();
    ServletContextEvent event = new ServletContextEvent(sc);
    listener.contextInitialized(event);
    String contextAttr = WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;
    applicationContext = (WebApplicationContext) sc.getAttribute(contextAttr);
    requestHandlerMappings =
        applicationContext.getBean(
            "requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
  }

  @Test
  public void shouldExtractRequestValuesWhenNoQueryParameters() {
    String method = "POST";
    String url = "http://localhost/users";
    String path = "/users";
    int status = HttpServletResponse.SC_CREATED;
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(status);
    SpringMvcDrivenHttpExtractor extractor = new SpringMvcDrivenHttpExtractor();
    extractor.setRequestHandlerMappings(requestHandlerMappings);
    assertEquals(method, extractor.getMethod(request));
    assertEquals(url, extractor.getUrl(request));
    assertEquals(path, extractor.getRoute(request));
    assertEquals(status, extractor.getStatusCode(response));
  }

  @Test
  public void shouldExtractRequestValuesWhenQueryParameters() {
    String method = "GET";
    String baseUrl = "http://localhost/users";
    String queryParams = "q=email:*example.org&limit=10&offset=1";
    String url = baseUrl + "?" + queryParams;
    String path = "/users";
    int status = HttpServletResponse.SC_OK;
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    request.setQueryString(queryParams);
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(status);
    SpringMvcDrivenHttpExtractor extractor = new SpringMvcDrivenHttpExtractor();
    extractor.setRequestHandlerMappings(requestHandlerMappings);
    assertEquals(method, extractor.getMethod(request));
    assertEquals(url, extractor.getUrl(request));
    assertEquals(path, extractor.getRoute(request));
    assertEquals(status, extractor.getStatusCode(response));
  }

  @Test
  public void shouldExtractRequestValuesWhenPathParameter() {
    String method = "GET";
    String url = "http://localhost/users/junit";
    String path = "/users/junit";
    String route = "/users/{userId}";
    int status = HttpServletResponse.SC_CREATED;
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(status);
    SpringMvcDrivenHttpExtractor extractor = new SpringMvcDrivenHttpExtractor();
    extractor.setRequestHandlerMappings(requestHandlerMappings);
    assertEquals(method, extractor.getMethod(request));
    assertEquals(url, extractor.getUrl(request));
    assertEquals(route, extractor.getRoute(request));
    assertEquals(status, extractor.getStatusCode(response));
  }

  @Test
  public void shouldReturnZeroIfResponseIsNull() {
    SpringMvcDrivenHttpExtractor extractor = new SpringMvcDrivenHttpExtractor();
    extractor.setRequestHandlerMappings(requestHandlerMappings);
    assertEquals(0, extractor.getStatusCode(null));
  }
}
