/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import com.google.common.collect.MapMaker;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Span processor to create new JFR events for the Span as they are started, and commit on end.
 *
 * <p>NOTE: JfrSpanProcessor must be running synchronously to ensure that duration is correctly
 * captured.
 */
public class JfrSpanProcessor implements SpanProcessor {

  private volatile Map<SpanContext, SpanEvent> spanEvents =
      new MapMaker().concurrencyLevel(16).initialCapacity(128).weakKeys().makeMap();

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    if (span.getSpanContext().isValid()) {
      SpanEvent event = new SpanEvent(span.toSpanData());
      event.begin();
      spanEvents.put(span.getSpanContext(), event);
    }
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan rs) {
    SpanEvent event = spanEvents.remove(rs.getSpanContext());
    if (event != null && event.shouldCommit()) {
      event.commit();
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public CompletableResultCode shutdown() {
    spanEvents = new NoopMap<>();
    return CompletableResultCode.ofSuccess();
  }

  private static class NoopMap<K, V> implements Map<K, V> {

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean containsKey(Object key) {
      return false;
    }

    @Override
    public boolean containsValue(Object value) {
      return false;
    }

    @Override
    public V get(Object key) {
      return null;
    }

    @Override
    public V put(K key, V value) {
      return null;
    }

    @Override
    public V remove(Object key) {
      return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {}

    @Override
    public void clear() {}

    @Override
    public Set<K> keySet() {
      return Collections.emptySet();
    }

    @Override
    public Collection<V> values() {
      return Collections.emptyList();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      return Collections.emptySet();
    }
  }
}
