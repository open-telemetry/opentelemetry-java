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

import static io.opentelemetry.contrib.http.servlet.AwxXrayHttpPropagationGetter.AMZNREQUESTIDKEY;
import static io.opentelemetry.contrib.http.servlet.AwxXrayHttpPropagationGetter.XAMZNREQUESTID;
import static io.opentelemetry.contrib.http.servlet.AwxXrayHttpPropagationGetter.XAMZNTRACEID;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACESTATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** Unit tests for {@link AwxXrayHttpPropagationGetter}. */
public class AwxXrayHttpPropagationGetterTest {

  @Test
  public void shouldProvideValuesIfRequestHasAwsXrayTraceIdWithApiGatewayParent() {
    AwxXrayHttpPropagationGetter getter = new AwxXrayHttpPropagationGetter();
    String xraytraceid =
        "Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1";
    String awsRequestId = "4a0f8f18-cb5f-11e0-8364-b14fdafc0888";
    String traceparentValue = "00-5759e988bd862e3fe1be46a994272793-53995c3f42cd8ad8-01";
    String tracestateValue = AMZNREQUESTIDKEY + "=" + awsRequestId;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(XAMZNTRACEID, xraytraceid);
    request.addHeader(XAMZNREQUESTID, awsRequestId);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertEquals(tracestateValue, getter.get(request, TRACESTATE));
    assertNull(getter.get(request, "X-Unknown"));
  }

  @Test
  public void shouldProvideValuesIfRequestHasAwsXrayTraceIdWithAlbSelf() {
    AwxXrayHttpPropagationGetter getter = new AwxXrayHttpPropagationGetter();
    String xraytraceid =
        "Self=1-67891234-012456789abcdef012345678;"
            + "Root=1-67891233-abcdef012345678912345678;CalledFrom=app";
    String awsRequestId = "624F92B7-0E5A-42F1-B7D2-904A5C24AEDB";
    String traceparentValue = "00-67891233abcdef012345678912345678-012456789abcdef0-00";
    String tracestateValue = AMZNREQUESTIDKEY + "=" + awsRequestId.toLowerCase();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(XAMZNTRACEID, xraytraceid);
    request.addHeader(XAMZNREQUESTID, awsRequestId);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertEquals(tracestateValue, getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldProvideValuesIfRequestHasAwsXrayTraceIdWithRootOnly() {
    AwxXrayHttpPropagationGetter getter = new AwxXrayHttpPropagationGetter();
    String xraytraceid = "Root=1-67891233-abcdef012345678912345678";
    String traceparentValue = "00-67891233abcdef012345678912345678-0000000000000004-00";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(XAMZNTRACEID, xraytraceid);
    assertTrue(getter.canProvideValues(request));
    assertEquals(traceparentValue, getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }

  @Test
  public void shouldNotProvideValuesIfRequestHasNoAwsXrayHeader() {
    AwxXrayHttpPropagationGetter getter = new AwxXrayHttpPropagationGetter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    assertFalse(getter.canProvideValues(request));
    assertNull(getter.get(request, TRACEPARENT));
    assertNull(getter.get(request, TRACESTATE));
  }
}
