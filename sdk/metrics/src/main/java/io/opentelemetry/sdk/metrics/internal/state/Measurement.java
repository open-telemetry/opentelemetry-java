package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;

public interface Measurement {
  long startEpochNanos();

  long epochNanos();

  boolean hasLongValue();

  long longValue();

  boolean hasDoubleValue();

  double doubleValue();

  Attributes attributes();
}
