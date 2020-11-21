/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraceMultiPropagatorTest {
  private static final TextMapPropagator PROPAGATOR1 = B3Propagator.getInstance();
  private static final TextMapPropagator PROPAGATOR2 =
      B3Propagator.builder().injectMultipleHeaders().build();
  private static final TextMapPropagator PROPAGATOR3 = HttpTraceContext.getInstance();

  private static final TextMapPropagator.Getter<Map<String, String>> getter =
      new TextMapPropagator.Getter<Map<String, String>>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  private static final Span SPAN =
      Span.wrap(
          SpanContext.createFromRemoteParent(
              TraceId.fromLongs(1245, 67890),
              SpanId.fromLong(12345),
              TraceFlags.getDefault(),
              TraceState.getDefault()));

  @Test
  void addPropagator_null() {
    assertThrows(
        NullPointerException.class, () -> TraceMultiPropagator.builder().addPropagator(null));
  }

  @Test
  void fields() {
    TextMapPropagator prop =
        TraceMultiPropagator.builder()
            .addPropagator(new EmptyPropagator("foo", "bar"))
            .addPropagator(new EmptyPropagator("hello", "world"))
            .build();

    List<String> fields = prop.fields();
    assertThat(fields).containsExactly("foo", "bar", "hello", "world");
  }

  @Test
  void fields_duplicates() {
    TextMapPropagator prop =
        TraceMultiPropagator.builder()
            .addPropagator(new EmptyPropagator("foo", "bar", "foo"))
            .addPropagator(new EmptyPropagator("hello", "world", "world", "bar"))
            .build();

    List<String> fields = prop.fields();
    assertThat(fields).containsExactly("foo", "bar", "hello", "world");
  }

  @Test
  void fields_readOnly() {
    TextMapPropagator prop =
        TraceMultiPropagator.builder()
            .addPropagator(new EmptyPropagator("foo", "bar"))
            .addPropagator(new EmptyPropagator("hello", "world"))
            .build();

    List<String> fields = prop.fields();
    assertThrows(UnsupportedOperationException.class, () -> fields.add("hi"));
  }

  @Test
  void inject_noPropagators() {
    TextMapPropagator prop = TraceMultiPropagator.builder().build();
    Map<String, String> carrier = new HashMap<>();

    Context context = Context.current();
    prop.inject(context, carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_allFormats() {
    TextMapPropagator prop =
        TraceMultiPropagator.builder()
            .addPropagator(PROPAGATOR1)
            .addPropagator(PROPAGATOR2)
            .addPropagator(PROPAGATOR3)
            .build();

    Map<String, String> carrier = new HashMap<>();
    prop.inject(Context.current().with(SPAN), carrier, Map::put);

    assertThat(
            Span.fromContext(PROPAGATOR1.extract(Context.current(), carrier, getter))
                .getSpanContext())
        .isEqualTo(SPAN.getSpanContext());
    assertThat(
            Span.fromContext(PROPAGATOR2.extract(Context.current(), carrier, getter))
                .getSpanContext())
        .isEqualTo(SPAN.getSpanContext());
    assertThat(
            Span.fromContext(PROPAGATOR3.extract(Context.current(), carrier, getter))
                .getSpanContext())
        .isEqualTo(SPAN.getSpanContext());
  }

  @Test
  void extract_noPropagators() {
    TextMapPropagator prop = TraceMultiPropagator.builder().build();
    Map<String, String> carrier = new HashMap<>();

    Context context = Context.current();
    Context resContext = prop.extract(context, carrier, getter);
    assertThat(context).isSameAs(resContext);
  }

  @Test
  void extract_found() {
    TextMapPropagator prop =
        TraceMultiPropagator.builder()
            .addPropagator(PROPAGATOR1)
            .addPropagator(PROPAGATOR2)
            .addPropagator(PROPAGATOR3)
            .build();

    Map<String, String> carrier = new HashMap<>();
    PROPAGATOR2.inject(Context.current().with(SPAN), carrier, Map::put);
    assertThat(Span.fromContext(prop.extract(Context.current(), carrier, getter)).getSpanContext())
        .isEqualTo(SPAN.getSpanContext());
  }

  @Test
  void extract_notFound() {
    TextMapPropagator prop = TraceMultiPropagator.builder().addPropagator(PROPAGATOR1).build();

    Map<String, String> carrier = new HashMap<>();
    PROPAGATOR3.inject(Context.current().with(SPAN), carrier, Map::put);
    assertThat(prop.extract(Context.current(), carrier, getter)).isEqualTo(Context.current());
  }

  @Test
  void extract_stopWhenFound() {
    TextMapPropagator mockPropagator = Mockito.mock(TextMapPropagator.class);
    TextMapPropagator prop =
        TraceMultiPropagator.builder()
            .addPropagator(mockPropagator)
            .addPropagator(PROPAGATOR3)
            .build();

    Map<String, String> carrier = new HashMap<>();
    PROPAGATOR3.inject(Context.current().with(SPAN), carrier, Map::put);
    assertThat(Span.fromContext(prop.extract(Context.current(), carrier, getter)).getSpanContext())
        .isEqualTo(SPAN.getSpanContext());
    verify(mockPropagator).fields();
    verifyNoMoreInteractions(mockPropagator);
  }

  private static class EmptyPropagator implements TextMapPropagator {
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
