/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * A {@link Context} implementation backed by {@link ScopedValue} for Java 25+.
 *
 * <p>Stores key-value pairs in the same flat {@code Object[]} array layout as {@link
 * ArrayBasedContext}, but binds the array via a single {@link ScopedValue} rather than a {@link
 * ThreadLocal}. This provides automatic propagation to virtual thread children when context is
 * established via any of the {@code wrap} methods.
 *
 * <p>{@link #makeCurrent()} falls back to a {@link ThreadLocal} bridge in {@link
 * ScopedValueContextStorage} to preserve open-scope semantics for existing callers.
 */
final class ScopedValueContext implements Context {

  // Single ScopedValue holding the current context — inherited by virtual thread children.
  static final ScopedValue<ScopedValueContext> SCOPED = ScopedValue.newInstance();

  private static final ScopedValueContext ROOT = new ScopedValueContext(new Object[0]);

  static ScopedValueContext root() {
    return ROOT;
  }

  // Same flat [key0, val0, key1, val1, ...] layout as ArrayBasedContext.
  private final Object[] entries;

  ScopedValueContext(Object[] entries) {
    this.entries = entries;
  }

  @Override
  @Nullable
  public <V> V get(ContextKey<V> key) {
    for (int i = 0; i < entries.length; i += 2) {
      if (entries[i] == key) {
        @SuppressWarnings("unchecked")
        V result = (V) entries[i + 1];
        return result;
      }
    }
    return null;
  }

  @Override
  public <V> Context with(ContextKey<V> key, V value) {
    for (int i = 0; i < entries.length; i += 2) {
      if (entries[i] == key) {
        if (entries[i + 1] == value) {
          return this;
        }
        Object[] newEntries = entries.clone();
        newEntries[i + 1] = value;
        return new ScopedValueContext(newEntries);
      }
    }
    Object[] newEntries = Arrays.copyOf(entries, entries.length + 2);
    newEntries[newEntries.length - 2] = key;
    newEntries[newEntries.length - 1] = value;
    return new ScopedValueContext(newEntries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < entries.length; i += 2) {
      sb.append(entries[i]).append('=').append(entries[i + 1]).append(", ");
    }
    if (sb.length() > 1) {
      sb.setLength(sb.length() - 2);
    }
    sb.append('}');
    return sb.toString();
  }

  // --- ScopedValue-backed wrap overrides ---
  //
  // Each override suspends any active makeCurrent() ThreadLocal override, establishes this context
  // via ScopedValue.where().run/call, then restores the ThreadLocal on exit. Virtual thread
  // children spawned inside the body inherit SCOPED automatically.
  //
  // wrap(Executor), wrap(ExecutorService), and wrap(ScheduledExecutorService) are NOT overridden:
  // their default implementations delegate to wrap(Runnable) / wrap(Callable), so they benefit
  // from the ScopedValue path automatically via polymorphism.

  @Override
  public Runnable wrap(Runnable runnable) {
    ScopedValueContext self = this;
    return () -> self.runInScope(runnable);
  }

  @Override
  public <T> Callable<T> wrap(Callable<T> callable) {
    ScopedValueContext self = this;
    return () -> self.callInScope(callable);
  }

  @Override
  public <T, U> Function<T, U> wrapFunction(Function<T, U> function) {
    ScopedValueContext self = this;
    return t -> {
      Object[] result = new Object[1];
      self.runInScope(() -> result[0] = function.apply(t));
      @SuppressWarnings("unchecked")
      U u = (U) result[0];
      return u;
    };
  }

  @Override
  public <T, U, V> BiFunction<T, U, V> wrapFunction(BiFunction<T, U, V> function) {
    ScopedValueContext self = this;
    return (t, u) -> {
      Object[] result = new Object[1];
      self.runInScope(() -> result[0] = function.apply(t, u));
      @SuppressWarnings("unchecked")
      V v = (V) result[0];
      return v;
    };
  }

  @Override
  public <T> Consumer<T> wrapConsumer(Consumer<T> consumer) {
    ScopedValueContext self = this;
    return t -> self.runInScope(() -> consumer.accept(t));
  }

  @Override
  public <T, U> BiConsumer<T, U> wrapConsumer(BiConsumer<T, U> consumer) {
    ScopedValueContext self = this;
    return (t, u) -> self.runInScope(() -> consumer.accept(t, u));
  }

  @Override
  public <T> Supplier<T> wrapSupplier(Supplier<T> supplier) {
    ScopedValueContext self = this;
    return () -> {
      Object[] result = new Object[1];
      self.runInScope(() -> result[0] = supplier.get());
      @SuppressWarnings("unchecked")
      T t = (T) result[0];
      return t;
    };
  }

  // Runs op inside the ScopedValue scope, suspending any active ThreadLocal override.
  private void runInScope(Runnable op) {
    Context prev = ScopedValueContextStorage.THREAD_LOCAL.get();
    ScopedValueContextStorage.THREAD_LOCAL.remove();
    try {
      ScopedValue.where(SCOPED, this).run(op);
    } finally {
      if (prev != null) {
        ScopedValueContextStorage.THREAD_LOCAL.set(prev);
      }
    }
  }

  // Variant of runInScope that returns a value and propagates checked exceptions.
  private <T> T callInScope(Callable<T> op) throws Exception {
    Context prev = ScopedValueContextStorage.THREAD_LOCAL.get();
    ScopedValueContextStorage.THREAD_LOCAL.remove();
    try {
      return ScopedValue.where(SCOPED, this).call(op);
    } finally {
      if (prev != null) {
        ScopedValueContextStorage.THREAD_LOCAL.set(prev);
      }
    }
  }
}
