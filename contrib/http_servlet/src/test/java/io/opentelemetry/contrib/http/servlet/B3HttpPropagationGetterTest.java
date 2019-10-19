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

import static io.opentelemetry.contrib.http.servlet.B3HttpPropagationGetter.B3COMBINED;
import static io.opentelemetry.contrib.http.servlet.B3HttpPropagationGetter.B3FLAGS;
import static io.opentelemetry.contrib.http.servlet.B3HttpPropagationGetter.B3PARENTSPANID;
import static io.opentelemetry.contrib.http.servlet.B3HttpPropagationGetter.B3PARENTSPANIDKEY;
import static io.opentelemetry.contrib.http.servlet.B3HttpPropagationGetter.B3SAMPLED;
import static io.opentelemetry.contrib.http.servlet.B3HttpPropagationGetter.B3SPANID;
import static io.opentelemetry.contrib.http.servlet.B3HttpPropagationGetter.B3TRACEID;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACESTATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** Unit tests for {@link B3HttpPropagationGetter}. */
public class B3HttpPropagationGetterTest {

  @Test
  public void shouldProvideValuesIfAllB3HeadersPresent() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    String b3TraceId = "463ac35c9f6413ad48485a3953bb6124";
    String b3SpanId = "a2fb4a1d1a96d312";
    String b3ParentSpanId = "0020000000000001";
    String b3Sampled = "1";
    String traceparentValue = "00-" + b3TraceId + "-" + b3SpanId + "-01";
    String tracestateValue = B3PARENTSPANIDKEY + "=" + b3ParentSpanId;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(B3TRACEID, b3TraceId);
    request.addHeader(B3SPANID, b3SpanId);
    request.addHeader(B3PARENTSPANID, b3ParentSpanId);
    request.addHeader(B3SAMPLED, b3Sampled);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertEquals(tracestateValue, getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldProvideValuesIf64BitB3TraceHeaderPresent() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    String b3TraceId = "463ac35c9f6413ad";
    String b3SpanId = "a2fb4a1d1a96d312";
    String traceparentValue = "00-0000000000000000" + b3TraceId + "-" + b3SpanId + "-00";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(B3TRACEID, b3TraceId);
    request.addHeader(B3SPANID, b3SpanId);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldProvideValuesIfB3CombinedHeaderPresent() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    String b3TraceId = "463ac35c9f6413ad48485a3953bb6124-a2fb4a1d1a96d312-1-0020000000000001";
    String traceparentValue = "00-463ac35c9f6413ad48485a3953bb6124-a2fb4a1d1a96d312-01";
    String tracestateValue = B3PARENTSPANIDKEY + "=0020000000000001";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(B3COMBINED, b3TraceId);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertEquals(tracestateValue, getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldNotProvideValuesIfRequestHasNoB3Headers() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    assertFalse(getter.canProvideValues(request));
    assertNull(getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldProvideProvideInvalidTraceIdIfOnlySampledValuePresent() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    String b3Sampled = "1";
    String traceparentValue = "00-00000000000000000000000000000000-0000000000000000-01";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(B3FLAGS, b3Sampled);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldProvideProvideInvalidTraceIdIfOnlyB3SampledValuePresent() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    String b3Sampled = "1";
    String traceparentValue = "00-00000000000000000000000000000000-0000000000000000-01";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(B3COMBINED, b3Sampled);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldReturnNullIfInvalidB3HeaderValue() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    String b3TraceId = "463ac35c9f6413ad48485a39-a2fb4a1d1a96d312-1-0020000000000001";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(B3COMBINED, b3TraceId);
    assertTrue(getter.canProvideValues(request));
    assertNull(getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldReturnNullIfB3TraceHeaderIsInvalid() {
    B3HttpPropagationGetter getter = new B3HttpPropagationGetter();
    String b3TraceId = "463ac35c9f641XYZ";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(B3TRACEID, b3TraceId);
    assertTrue(getter.canProvideValues(request));
    assertNull(getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }
}
