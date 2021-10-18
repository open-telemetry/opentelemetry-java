package io.opentelemetry.sdk.metrics.internal.state;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MapCounter implements ExponentialCounter {

  private static final long NULL_INDEX = Long.MIN_VALUE;

  private final Map<Integer, AtomicLong> backing;
  private final int maxSize;
  private long indexStart;
  private long indexEnd;

  public MapCounter(int maxSize) {
    this.backing = new HashMap<>(maxSize);
    this.indexEnd = NULL_INDEX;
    this.indexStart = NULL_INDEX;
    this.maxSize = maxSize;
  }

  @Override
  public long getIndexStart() {
    return indexStart;
  }

  @Override
  public long getIndexEnd() {
    return indexEnd;
  }

  @Override
  public boolean increment(long index, long delta) {
    // todo verify do we actually need to restrict this?
    if(index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
      return false;
    }
    int i = (int) index; // safely castable due to above check
    if (indexStart == NULL_INDEX) {
      indexStart = index;
      indexEnd = index;
      doIncrement(i, delta);
      return true;
    }

    // Extend window if possible. if it would exceed maxSize, then return false.
    if (i > indexEnd) {
      if (i - indexStart + 1 > maxSize) {
        return false;
      }
      indexEnd = i;
    } else if (i < indexStart) {
      if (indexEnd - i + 1 > maxSize) {
        return false;
      }
      indexStart = i;
    }

    doIncrement(i, delta);
    return true;
  }

  @Override
  public long get(long index) {
    if (index < indexStart || index > indexEnd) {
      throw new IndexOutOfBoundsException(String.format("Index %d out of range.", index));
    }
    AtomicLong result = backing.get((int) index);
    if (result == null) {
      return 0;
    }
    return result.longValue();
  }

  @Override
  public boolean isEmpty() {
    return backing.isEmpty();
  }

  private void doIncrement(int index, long delta) {
    AtomicLong value = backing.get(index);
    if (value == null) {
      backing.put(index, new AtomicLong(delta));
    } else {
      value.getAndAdd(delta);
    }
  }
}
