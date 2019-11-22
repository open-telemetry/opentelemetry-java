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

import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.contrib.http.core.HttpStatus2OtStatusConverter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Tracer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
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

/** Unit tests for {@link OtelJaxrsClientFilter}. */
public class OtelJaxrsClientFilterTest {

  private static final Logger LOGGER = Logger.getLogger(OtelJaxrsClientFilter.class.getName());
  private static final Tracer TRACER = OpenTelemetry.getTracerFactory().get(INSTRUMENTATION_LIB_ID);

  @Test
  public void shouldStartNewSpanAndInjectPropagationDataOnClientCall() {
    OtelJaxrsClientFilter filter =
        new OtelJaxrsClientFilter(
            new UrlPathDrivenJaxrsClientHttpExtractor(),
            new HttpStatus2OtStatusConverter(),
            TRACER);
    try (Scope parent = startParentScope()) {
      ClientRequestContext requestContext = constructRequestContext();
      ClientResponseContext responseContext = constructResponseContext();
      filter.filter(requestContext);
      String traceparent = requestContext.getHeaderString("traceparent");
      LOGGER.info("REST call with traceparent=" + traceparent);
      Span span = TRACER.getCurrentSpan();
      assertTrue(span.getContext().isValid());
      SpanId clientSpanId = span.getContext().getSpanId();
      assertTrue(traceparent.contains(clientSpanId.toLowerBase16()));
      filter.filter(requestContext, responseContext);
      SpanId parentSpanId = TRACER.getCurrentSpan().getContext().getSpanId();
      assertNotEquals(clientSpanId, parentSpanId);
    }
  }

  @Test
  public void shouldStartTraceIfNotCurrentlyExists() {
    OtelJaxrsClientFilter filter = new OtelJaxrsClientFilter();
    ClientRequestContext requestContext = constructRequestContext();
    ClientResponseContext responseContext = constructResponseContext();
    filter.filter(requestContext);
    String traceparent = requestContext.getHeaderString("traceparent");
    LOGGER.info("REST call with traceparent=" + traceparent);
    Span span = TRACER.getCurrentSpan();
    assertTrue(span.getContext().isValid());
    SpanId clientSpanId = span.getContext().getSpanId();
    assertTrue(traceparent.contains(clientSpanId.toLowerBase16()));
    filter.filter(requestContext, responseContext);
  }

  @SuppressWarnings("MustBeClosedChecker")
  private static Scope startParentScope() {
    Span span = TRACER.spanBuilder("/api-call").setSpanKind(Span.Kind.SERVER).startSpan();
    return TRACER.withSpan(span);
  }

  private static ClientRequestContext constructRequestContext() {
    MessageImpl reqMessage = new MessageImpl();
    reqMessage.setExchange(new ExchangeImpl());
    reqMessage.put(Message.PROTOCOL_HEADERS, new MultivaluedHashMap<String, String>());
    ClientRequestContextImpl request = new ClientRequestContextImpl(reqMessage, false);
    URI uri = null;
    try {
      uri = new URI("https://api.swacrew.com/stations/DAL");
    } catch (URISyntaxException cause) {
      throw new IllegalStateException(cause);
    }
    request.setUri(uri);
    request.setMethod("GET");
    request.getHeaders().add("user-agent", "PostmanRuntime/7.15.0");
    return request;
  }

  private static ClientResponseContext constructResponseContext() {
    ResponseBuilderImpl builder = new ResponseBuilderImpl();
    builder.status(Response.Status.OK);
    ClientResponseContextImpl response =
        new ClientResponseContextImpl((ResponseImpl) builder.build(), new MessageImpl());
    return response;
  }
}
