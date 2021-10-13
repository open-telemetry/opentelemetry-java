package io.opentelemetry.sdk.metrics.internal.state;

/**
 * Interface for use as backing data structure for exponential histogram buckets.
 */
public interface ExponentialCounter {
  long getIndexStart();
  long getIndexEnd();
  boolean increment(long index, long delta);
  long get(long index);
  boolean isEmpty();
}
