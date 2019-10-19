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

import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.ALL_SCHEMES;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACESTATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;

/** Unit tests for {@link MultiSchemeHttpPropagationGetter}. */
public class MultiSchemeHttpPropagationGetterTest {

  @Rule public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void shouldInitializeAndUseAllInternallySupportedSchemes() {
    MultiSchemeHttpPropagationGetter getter = new MultiSchemeHttpPropagationGetter(ALL_SCHEMES);
    getter.initializeGetters();
    String traceparentValue = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
    String tracestateValue = "rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TRACEPARENT, traceparentValue);
    request.addHeader(TRACESTATE, tracestateValue);
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertEquals(tracestateValue, getter.get(request, TRACESTATE));
    assertNull(getter.get(request, "X-Unknown"));
  }

  @Test
  public void shouldInitializeAndUseBothInternalAndAdditionalSchemes() {
    MultiSchemeHttpPropagationGetter getter =
        new MultiSchemeHttpPropagationGetter(W3cTraceContextHttpPropagationGetter.SCHEME_NAME);
    getter.initializeGetters(new TestOnlyHttpPropagationGetter());
    String testOnlyHeader = "X-TestOnly";
    String testOnlyValue = "F165E0BB-9E29-4212-898D-942640D3F1B6";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(testOnlyHeader, testOnlyValue);
    assertEquals(testOnlyValue, getter.get(request, testOnlyHeader));
  }

  @Test
  public void shouldReturnNullIfNoGettersCanHandleRequest() {
    MultiSchemeHttpPropagationGetter getter =
        new MultiSchemeHttpPropagationGetter(B3HttpPropagationGetter.SCHEME_NAME);
    getter.initializeGetters();
    String traceparentValue = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TRACEPARENT, traceparentValue);
    assertNull(getter.get(request, TRACEPARENT));
  }

  @Test
  public void shouldThrowExceptionIfNoSchemeSpecificGettersSpecified() {
    MultiSchemeHttpPropagationGetter getter = new MultiSchemeHttpPropagationGetter(null);
    expectedException.expect(IllegalStateException.class);
    getter.initializeGetters();
  }

  @Test
  public void shouldThrowExceptionIfGettersHaveNotBeenInitialized() {
    MultiSchemeHttpPropagationGetter getter = new MultiSchemeHttpPropagationGetter(ALL_SCHEMES);
    String traceparentValue = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TRACEPARENT, traceparentValue);
    expectedException.expect(IllegalStateException.class);
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
  }
}
