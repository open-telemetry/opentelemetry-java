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

import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACESTATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** Unit tests for {@link W3cTraceContextHttpPropagationGetter}. */
public class W3cTraceContextHttpPropagationGetterTest {

  @Test
  public void shouldProvideValuesIfRequestHasW3cTraceContextHeaders() {
    W3cTraceContextHttpPropagationGetter getter = new W3cTraceContextHttpPropagationGetter();
    String traceparentValue = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
    String tracestateValue = "rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TRACEPARENT, traceparentValue);
    request.addHeader(TRACESTATE, tracestateValue);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertEquals(tracestateValue, getter.get(request, TRACESTATE));
    assertNull(getter.get(request, "X-Unknown"));
  }

  @Test
  public void shouldNotProvideValuesIfRequestHasOnlyAwsXrayHeaders() {
    W3cTraceContextHttpPropagationGetter getter = new W3cTraceContextHttpPropagationGetter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(
        "X-Amzn-Trace-Id",
        "Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1");
    assertFalse(getter.canProvideValues(request));
    assertNull(getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldReturnEmptyIfHeaderValueIsInvalid() {
    W3cTraceContextHttpPropagationGetter getter = new W3cTraceContextHttpPropagationGetter();
    String traceparentValue = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f0";
    String tracestateValue = "rojo=00f067aa0ba902b7,=t61rcWkgMzE";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TRACEPARENT, traceparentValue);
    request.addHeader(TRACESTATE, tracestateValue);
    assertTrue(getter.canProvideValues(request));
    assertNull(getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }
}
