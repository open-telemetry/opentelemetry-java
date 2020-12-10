/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.context.Context;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MultiTextMapPropagatorTest {

  @Mock private TextMapPropagator propagator1;
  @Mock private TextMapPropagator propagator2;
  @Mock private TextMapPropagator propagator3;

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

  @Test
  void addPropagator_null() {
    assertThrows(
        NullPointerException.class, () -> MultiTextMapPropagator.create((TextMapPropagator) null));
  }

  @Test
  void fields() {
    when(propagator1.fields()).thenReturn(Arrays.asList("foo", "bar"));
    when(propagator2.fields()).thenReturn(Arrays.asList("hello", "world"));
    TextMapPropagator prop = MultiTextMapPropagator.create(propagator1, propagator2);

    Collection<String> fields = prop.fields();
    assertThat(fields).containsExactly("foo", "bar", "hello", "world");
  }

  @Test
  void fields_duplicates() {
    when(propagator1.fields()).thenReturn(Arrays.asList("foo", "bar", "foo"));
    when(propagator2.fields()).thenReturn(Arrays.asList("hello", "world", "world", "bar"));
    TextMapPropagator prop = MultiTextMapPropagator.create(propagator1, propagator2);

    Collection<String> fields = prop.fields();
    assertThat(fields).containsExactly("foo", "bar", "hello", "world");
  }

  @Test
  void fields_readOnly() {
    when(propagator1.fields()).thenReturn(Arrays.asList("rubber", "baby"));
    when(propagator2.fields()).thenReturn(Arrays.asList("buggy", "bumpers"));
    TextMapPropagator prop = MultiTextMapPropagator.create(propagator1, propagator2);
    Collection<String> fields = prop.fields();
    assertThrows(UnsupportedOperationException.class, () -> fields.add("hi"));
  }

  @Test
  void inject_noPropagators() {
    TextMapPropagator prop = MultiTextMapPropagator.create();
    Map<String, String> carrier = new HashMap<>();

    Context context = Context.current();
    prop.inject(context, carrier, Map::put);
    assertThat(carrier).isEmpty();
    assertThat(prop).isSameAs(NoopTextMapPropagator.getInstance());
  }

  @Test
  void inject_allDelegated() {
    Map<String, String> carrier = new HashMap<>();
    Context context = mock(Context.class);
    TextMapPropagator.Setter<Map<String, String>> setter = Map::put;

    TextMapPropagator prop = MultiTextMapPropagator.create(propagator1, propagator2, propagator3);
    prop.inject(context, carrier, setter);
    verify(propagator1).inject(context, carrier, setter);
    verify(propagator2).inject(context, carrier, setter);
    verify(propagator3).inject(context, carrier, setter);
  }

  @Test
  void extract_noPropagators() {
    Map<String, String> carrier = new HashMap<>();
    Context context = mock(Context.class);

    TextMapPropagator prop = MultiTextMapPropagator.create();
    Context resContext = prop.extract(context, carrier, getter);
    assertThat(context).isSameAs(resContext);
  }

  @Test
  void extract_found_all() {
    Map<String, String> carrier = new HashMap<>();
    TextMapPropagator prop = MultiTextMapPropagator.create(propagator1, propagator2, propagator3);
    Context context1 = mock(Context.class);
    Context context2 = mock(Context.class);
    Context context3 = mock(Context.class);
    Context expectedContext = mock(Context.class);

    when(propagator1.extract(context1, carrier, getter)).thenReturn(context2);
    when(propagator2.extract(context2, carrier, getter)).thenReturn(context3);
    when(propagator3.extract(context3, carrier, getter)).thenReturn(expectedContext);

    assertThat(prop.extract(context1, carrier, getter)).isEqualTo(expectedContext);
  }

  @Test
  void extract_notFound() {

    Map<String, String> carrier = new HashMap<>();
    Context context = mock(Context.class);
    when(propagator1.extract(context, carrier, getter)).thenReturn(context);
    when(propagator2.extract(context, carrier, getter)).thenReturn(context);

    TextMapPropagator prop = MultiTextMapPropagator.create(propagator1, propagator2);
    Context result = prop.extract(context, carrier, getter);

    assertThat(result).isSameAs(context);
  }

  @Test
  void extract_stopWhenFound() {
    Map<String, String> carrier = new HashMap<>();
    Context context = mock(Context.class);
    Context updatedContext = mock(Context.class);

    when(propagator1.extract(context, carrier, getter)).thenReturn(context);
    when(propagator2.extract(context, carrier, getter)).thenReturn(updatedContext);
    TextMapPropagator prop =
        MultiTextMapPropagator.builder(propagator1, propagator2, propagator3)
            .stopExtractAfterFirst()
            .build();

    Context result = prop.extract(context, carrier, getter);
    assertThat(result).isSameAs(updatedContext);
    verify(propagator3).fields();
    verifyNoMoreInteractions(propagator3);
  }

  @Test
  void extract_stopWhenFoundBackward() {
    Map<String, String> carrier = new HashMap<>();
    Context context = mock(Context.class);
    Context updatedContext = mock(Context.class);

    when(propagator3.extract(context, carrier, getter)).thenReturn(context);
    when(propagator2.extract(context, carrier, getter)).thenReturn(updatedContext);
    TextMapPropagator prop =
        MultiTextMapPropagator.createBackwards(propagator1, propagator2, propagator3);

    Context result = prop.extract(context, carrier, getter);
    assertThat(result).isSameAs(updatedContext);
    verify(propagator1).fields();
    verifyNoMoreInteractions(propagator1);
  }

  @Test
  void createWithOneDelegate() {
    TextMapPropagator result = MultiTextMapPropagator.create(propagator2);
    assertThat(result).isSameAs(propagator2);
  }
}
