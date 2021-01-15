/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class DoubleSumData implements SumData<DoublePointData> {
  DoubleSumData() {}

  public static DoubleSumData create(
      boolean isMonotonic, AggregationTemporality temporality, Collection<DoublePointData> points) {
    return new AutoValue_DoubleSumData(points, isMonotonic, temporality);
  }
}
