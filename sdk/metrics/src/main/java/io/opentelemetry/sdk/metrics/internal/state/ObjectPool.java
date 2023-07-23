package io.opentelemetry.sdk.metrics.internal.state;

import java.util.function.Supplier;

/**
 * A pool of objects of type {@code T}
 *
 * Not thread safe
 */
public class ObjectPool<T> {
  private final ArrayBasedStack<T> pool;
  private final Supplier<T> creator;

  private static final java.util.concurrent.atomic.LongAdder objectsCreated = new java.util.concurrent.atomic.LongAdder();
  private static final java.util.concurrent.atomic.LongAdder objectsBorrowed = new java.util.concurrent.atomic.LongAdder();
  private static final java.util.concurrent.atomic.LongAdder objectsReturned = new java.util.concurrent.atomic.LongAdder();

  @SuppressWarnings("SystemOut")
  public static void printReport() {
    System.out.println("objectsCreated = " + objectsCreated.longValue());
    System.out.println("objectsBorrowed = " + objectsBorrowed.longValue());
    System.out.println("objectsReturned = " + objectsReturned.longValue());
  }

  public ObjectPool(Supplier<T> creator) {
    this.pool = new ArrayBasedStack<>();
    this.creator = creator;
  }

  /**
   * Gets an object from the pool
   *
   * @return An object from the pool, or a new object if the pool is empty
   */
  public T borrowObject() {
    T object = pool.pop();
    if (object == null) {
      object = creator.get();
      objectsCreated.increment();
    } else {
      objectsBorrowed.increment();
    }
    return object;
  }

  public void returnObject(T object) {
    pool.push(object);
    objectsReturned.increment();
  }
}
