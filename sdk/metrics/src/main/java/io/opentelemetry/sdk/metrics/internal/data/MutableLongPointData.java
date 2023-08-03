/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MutableLongPointData implements LongPointData {

  private long value;
  private long startEpochNanos;
  private long epochNanos;

  @Nullable
  private Attributes attributes;

  private List<LongExemplarData> exemplars = Collections.emptyList();

  @Override
  public long getValue() {
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
  @SuppressWarnings({"NullAway", "NullableProblems"})
  @Override
  public Attributes getAttributes() {
    return attributes;
  }

  @Override
  public List<LongExemplarData> getExemplars() {
    return exemplars;
  }

  public void set(LongPointData point) {
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
      long value) {
    set(startEpochNanos, epochNanos, attributes, value, Collections.emptyList());
  }

  public void set(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      long value,
      List<LongExemplarData> exemplars) {
    this.startEpochNanos = startEpochNanos;
    this.epochNanos = epochNanos;
    this.attributes = attributes;
    this.value = value;
    this.exemplars = exemplars;
  }

  @Override
  public String toString() {
    return "MutableLongPointData{" +
        "value=" + value +
        ", startEpochNanos=" + startEpochNanos +
        ", epochNanos=" + epochNanos +
        ", attributes=" + attributes +
        ", exemplars=" + exemplars +
        '}';
  }
}
