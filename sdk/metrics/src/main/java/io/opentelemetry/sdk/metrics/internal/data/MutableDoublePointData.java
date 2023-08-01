/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MutableDoublePointData implements DoublePointData {

  private long startEpochNanos;
  private long epochNanos;

  @Nullable
  private Attributes attributes;

  private double value;
  private List<DoubleExemplarData> exemplars = Collections.emptyList();

  @Override
  public double getValue() {
    return value;
  }

  @Override
  public long getStartEpochNanos() {
    return startEpochNanos;
  }

  @Override
  public long getEpochNanos() {
    return epochNanos;
  }


  // attributes is null only upon initial creation and never returned as such
  @SuppressWarnings("NullAway")
  @Override
  public Attributes getAttributes() {
    return attributes;
  }

  @Override
  public List<DoubleExemplarData> getExemplars() {
    return exemplars;
  }

  public void set(DoublePointData point) {
    set(point.getStartEpochNanos(),
        point.getEpochNanos(),
        point.getAttributes(),
        point.getValue(),
        point.getExemplars());
  }

  public void set(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double value) {
    set(startEpochNanos, epochNanos, attributes, value, Collections.emptyList());
  }

  public void set(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double value,
      List<DoubleExemplarData> exemplars) {
    this.startEpochNanos = startEpochNanos;
    this.epochNanos = epochNanos;
    this.attributes = attributes;
    this.value = value;
    this.exemplars = exemplars;
  }

  public void setValue(double value) {
    this.value = value;
  }
}
