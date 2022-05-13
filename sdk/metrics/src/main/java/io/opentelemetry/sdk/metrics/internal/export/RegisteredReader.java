/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

/**
 * Represents a {@link MetricReader} registered with {@link SdkMeterProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class RegisteredReader {

  private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);
  private final int id = ID_COUNTER.incrementAndGet();
  private final MetricReader metricReader;

  /** Construct a new collection info object storing information for collection against a reader. */
  public static RegisteredReader create(MetricReader reader) {
    return new RegisteredReader(reader);
  }

  private RegisteredReader(MetricReader metricReader) {
    this.metricReader = metricReader;
  }

  public MetricReader getReader() {
    return metricReader;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegisteredReader)) {
      return false;
    }
    return id == ((RegisteredReader) o).id;
  }

  @Override
  public String toString() {
    return "RegisteredReader{" + id + "}";
  }
}
