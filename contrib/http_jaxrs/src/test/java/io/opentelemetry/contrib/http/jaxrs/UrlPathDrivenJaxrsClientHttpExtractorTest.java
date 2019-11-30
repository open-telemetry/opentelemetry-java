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

package io.opentelemetry.contrib.http.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.spec.ClientRequestContextImpl;
import org.apache.cxf.jaxrs.client.spec.ClientResponseContextImpl;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.junit.Test;

/** Unit tests for {@link UrlPathDrivenJaxrsClientHttpExtractor}. */
public class UrlPathDrivenJaxrsClientHttpExtractorTest {

  @Test
  public void shouldExtractValuesFromRequestAndResponse() throws URISyntaxException {
    MessageImpl reqMessage = new MessageImpl();
    reqMessage.setExchange(new ExchangeImpl());
    reqMessage.put(Message.PROTOCOL_HEADERS, new MultivaluedHashMap<String, String>());
    ClientRequestContextImpl request = new ClientRequestContextImpl(reqMessage, false);
    ResponseBuilderImpl builder = new ResponseBuilderImpl();
    builder.status(Response.Status.OK);
    ClientResponseContextImpl response =
        new ClientResponseContextImpl((ResponseImpl) builder.build(), new MessageImpl());
    UrlPathDrivenJaxrsClientHttpExtractor extractor = new UrlPathDrivenJaxrsClientHttpExtractor();
    URI uri = new URI("https://api.example.com/locations/DAL");
    String method = "GET";
    String userAgent = "PostmanRuntime/7.15.0";
    request.setUri(uri);
    request.setMethod(method);
    request.getHeaders().add("user-agent", userAgent);
    assertEquals(uri.getPath(), extractor.getRoute(request));
    assertEquals(uri.toString(), extractor.getUrl(request));
    assertEquals(method, extractor.getMethod(request));
    assertNull(extractor.getHttpFlavor(request));
    assertNull(extractor.getClientIp(request));
    assertEquals(userAgent, extractor.getUserAgent(request));
    assertEquals(Response.Status.OK.getStatusCode(), extractor.getStatusCode(response));
  }

  @Test
  public void shouldHandleNullResponse() {
    UrlPathDrivenJaxrsClientHttpExtractor extractor = new UrlPathDrivenJaxrsClientHttpExtractor();
    assertEquals(0, extractor.getStatusCode(null));
  }
}
