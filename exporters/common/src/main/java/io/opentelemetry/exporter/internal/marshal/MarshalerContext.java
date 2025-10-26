/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Class for keeping marshaling state. The state consists of integers, that we call sizes, and
 * objects, that we call data. Both integers and objects can be read from the state in the order
 * they were added (first in, first out). Additionally, this class provides various pools and caches
 * for objects that can be reused between marshalling attempts.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class MarshalerContext {
  private final boolean marshalStringNoAllocation;
  private final StringEncoder stringEncoder;

  private int[] sizes = new int[16];
  private int sizeReadIndex;
  private int sizeWriteIndex;
  private Object[] data = new Object[16];
  private int dataReadIndex;
  private int dataWriteIndex;

  public MarshalerContext() {
    this(/* marshalStringNoAllocation= */ true);
  }

  public MarshalerContext(boolean marshalStringNoAllocation) {
    this.marshalStringNoAllocation = marshalStringNoAllocation;
    this.stringEncoder = StringEncoder.getInstance();
  }

  public MarshalerContext(boolean marshalStringNoAllocation, StringEncoder stringEncoder) {
    this.marshalStringNoAllocation = marshalStringNoAllocation;
    this.stringEncoder = stringEncoder;
  }

  public boolean marshalStringNoAllocation() {
    return marshalStringNoAllocation;
  }

  public StringEncoder getStringEncoder() {
    return stringEncoder;
  }

  public void addSize(int size) {
    growSizeIfNeeded();
    sizes[sizeWriteIndex++] = size;
  }

  public int addSize() {
    growSizeIfNeeded();
    return sizeWriteIndex++;
  }

  private void growSizeIfNeeded() {
    if (sizeWriteIndex == sizes.length) {
      int[] newSizes = new int[sizes.length * 2];
      System.arraycopy(sizes, 0, newSizes, 0, sizes.length);
      sizes = newSizes;
    }
  }

  public void setSize(int index, int size) {
    sizes[index] = size;
  }

  public int getSize() {
    return sizes[sizeReadIndex++];
  }

  public void addData(@Nullable Object o) {
    growDataIfNeeded();
    data[dataWriteIndex++] = o;
  }

  private void growDataIfNeeded() {
    if (dataWriteIndex == data.length) {
      Object[] newData = new Object[data.length * 2];
      System.arraycopy(data, 0, newData, 0, data.length);
      data = newData;
    }
  }

  public <T> T getData(Class<T> type) {
    return type.cast(data[dataReadIndex++]);
  }

  private final IdPool traceIdPool = new IdPool(TraceId.getLength() / 2);

  /** Returns a buffer that can be used to hold a trace id. */
  public byte[] getTraceIdBuffer() {
    return traceIdPool.get();
  }

  private final IdPool spanIdPool = new IdPool(SpanId.getLength() / 2);

  /** Returns a buffer that can be used to hold a span id. */
  public byte[] getSpanIdBuffer() {
    return spanIdPool.get();
  }

  private static class IdPool {
    private final List<byte[]> pool = new ArrayList<>();
    int index;
    final int idSize;

    IdPool(int idSize) {
      this.idSize = idSize;
    }

    byte[] get() {
      if (index < pool.size()) {
        return pool.get(index++);
      }
      byte[] result = new byte[idSize];
      pool.add(result);
      index++;

      return result;
    }

    void reset() {
      index = 0;
    }
  }

  private final Pool<Map<?, ?>> mapPool = new Pool<>(IdentityHashMap::new, Map::clear);

  /** Returns a pooled identity map. */
  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> getIdentityMap() {
    return (Map<K, V>) mapPool.get();
  }

  private final Pool<List<?>> listPool = new Pool<>(ArrayList::new, List::clear);

  /** Returns a pooled list. */
  @SuppressWarnings("unchecked")
  public <T> List<T> getList() {
    return (List<T>) listPool.get();
  }

  private static class Pool<T> {
    private final List<T> pool = new ArrayList<>();
    private int index;
    private final Supplier<T> factory;
    private final Consumer<T> clean;

    Pool(Supplier<T> factory, Consumer<T> clean) {
      this.factory = factory;
      this.clean = clean;
    }

    T get() {
      if (index < pool.size()) {
        return pool.get(index++);
      }
      T result = factory.get();
      pool.add(result);
      index++;

      return result;
    }

    void reset() {
      for (int i = 0; i < index; i++) {
        clean.accept(pool.get(i));
      }
      index = 0;
    }
  }

  /** Reset context so that serialization could be re-run. */
  public void resetReadIndex() {
    sizeReadIndex = 0;
    dataReadIndex = 0;
  }

  /** Reset context so that it could be reused. */
  public void reset() {
    sizeReadIndex = 0;
    sizeWriteIndex = 0;
    for (int i = 0; i < dataWriteIndex; i++) {
      data[i] = null;
    }
    dataReadIndex = 0;
    dataWriteIndex = 0;

    traceIdPool.reset();
    spanIdPool.reset();

    mapPool.reset();
    listPool.reset();
  }

  private static final AtomicInteger KEY_INDEX = new AtomicInteger();

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  public static class Key {
    final int index = KEY_INDEX.getAndIncrement();
  }

  public static Key key() {
    return new Key();
  }

  private Object[] instances = new Object[16];

  @SuppressWarnings("unchecked")
  public <T> T getInstance(Key key, Supplier<T> supplier) {
    if (key.index >= instances.length) {
      Object[] newData = new Object[instances.length * 2];
      System.arraycopy(instances, 0, newData, 0, instances.length);
      instances = newData;
    }

    T result = (T) instances[key.index];
    if (result == null) {
      result = supplier.get();
      instances[key.index] = result;
    }
    return result;
  }
}
