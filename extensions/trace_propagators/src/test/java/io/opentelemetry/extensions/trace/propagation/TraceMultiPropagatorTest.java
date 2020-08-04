/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.extensions.trace.propagation;

import static io.opentelemetry.trace.TracingContextUtils.getSpan;
import static io.opentelemetry.trace.TracingContextUtils.withSpan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class TraceMultiPropagatorTest {
  private static final HttpTextFormat PROPAGATOR1 = B3Propagator.getSingleHeaderPropagator();
  private static final HttpTextFormat PROPAGATOR2 = B3Propagator.getMultipleHeaderPropagator();
  private static final HttpTextFormat PROPAGATOR3 = new HttpTraceContext();

  private static final Span SPAN =
      DefaultSpan.create(
          SpanContext.createFromRemoteParent(
              new TraceId(1245, 67890),
              new SpanId(12345),
              TraceFlags.getDefault(),
              TraceState.getDefault()));

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void addPropagator_null() {
    assertThrows(
        NullPointerException.class, () -> TraceMultiPropagator.builder().addPropagator(null));
  }

  @Test
  void fields() {
    HttpTextFormat prop =
        TraceMultiPropagator.builder()
            .addPropagator(new EmptyPropagator("foo", "bar"))
            .addPropagator(new EmptyPropagator("hello", "world"))
            .build();

    List<String> fields = prop.fields();
    assertThat(fields).hasSize(4);
    assertThat(fields).isEqualTo(Arrays.asList("foo", "bar", "hello", "world"));
  }

  @Test
  void fields_readOnly() {
    HttpTextFormat prop =
        TraceMultiPropagator.builder()
            .addPropagator(new EmptyPropagator("foo", "bar"))
            .addPropagator(new EmptyPropagator("hello", "world"))
            .build();

    List<String> fields = prop.fields();
    assertThrows(UnsupportedOperationException.class, () -> fields.add("hi"));
  }

  @Test
  void inject_noPropagators() {
    HttpTextFormat prop = TraceMultiPropagator.builder().build();
    Map<String, String> carrier = new HashMap<>();

    Context context = Context.current();
    prop.inject(context, carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_allFormats() {
    HttpTextFormat prop =
        TraceMultiPropagator.builder()
            .addPropagator(PROPAGATOR1)
            .addPropagator(PROPAGATOR2)
            .addPropagator(PROPAGATOR3)
            .build();

    Map<String, String> carrier = new HashMap<>();
    prop.inject(withSpan(SPAN, Context.current()), carrier, Map::put);

    assertThat(getSpan(PROPAGATOR1.extract(Context.current(), carrier, Map::get)).getContext())
        .isEqualTo(SPAN.getContext());
    assertThat(getSpan(PROPAGATOR2.extract(Context.current(), carrier, Map::get)).getContext())
        .isEqualTo(SPAN.getContext());
    assertThat(getSpan(PROPAGATOR3.extract(Context.current(), carrier, Map::get)).getContext())
        .isEqualTo(SPAN.getContext());
  }

  @Test
  void extract_noPropagators() {
    HttpTextFormat prop = TraceMultiPropagator.builder().build();
    Map<String, String> carrier = new HashMap<>();

    Context context = Context.current();
    Context resContext = prop.extract(context, carrier, Map::get);
    assertThat(context).isSameAs(resContext);
  }

  @Test
  void extract_found() {
    HttpTextFormat prop =
        TraceMultiPropagator.builder()
            .addPropagator(PROPAGATOR1)
            .addPropagator(PROPAGATOR2)
            .addPropagator(PROPAGATOR3)
            .build();

    Map<String, String> carrier = new HashMap<>();
    PROPAGATOR2.inject(withSpan(SPAN, Context.current()), carrier, Map::put);
    assertThat(getSpan(prop.extract(Context.current(), carrier, Map::get)).getContext())
        .isEqualTo(SPAN.getContext());
  }

  @Test
  void extract_notFound() {
    HttpTextFormat prop = TraceMultiPropagator.builder().addPropagator(PROPAGATOR1).build();

    Map<String, String> carrier = new HashMap<>();
    PROPAGATOR3.inject(withSpan(SPAN, Context.current()), carrier, Map::put);
    assertThat(prop.extract(Context.current(), carrier, Map::get)).isEqualTo(Context.current());
  }

  @Test
  void extract_stopWhenFound() {
    HttpTextFormat mockPropagator = Mockito.mock(HttpTextFormat.class);
    HttpTextFormat prop =
        TraceMultiPropagator.builder()
            .addPropagator(mockPropagator)
            .addPropagator(PROPAGATOR3)
            .build();

    Map<String, String> carrier = new HashMap<>();
    PROPAGATOR3.inject(withSpan(SPAN, Context.current()), carrier, Map::put);
    assertThat(getSpan(prop.extract(Context.current(), carrier, Map::get)).getContext())
        .isEqualTo(SPAN.getContext());
    verify(mockPropagator).fields();
    verifyNoMoreInteractions(mockPropagator);
  }

  private static class EmptyPropagator implements HttpTextFormat {
    List<String> fields;

    public EmptyPropagator(String... fields) {
      this.fields = Arrays.asList(fields);
    }

    @Override
    public List<String> fields() {
      return fields;
    }

    @Override
    public <C> void inject(Context context, C carrier, Setter<C> c) {}

    @Override
    public <C> Context extract(Context context, C carrier, Getter<C> c) {
      return context;
    }
  }
}
