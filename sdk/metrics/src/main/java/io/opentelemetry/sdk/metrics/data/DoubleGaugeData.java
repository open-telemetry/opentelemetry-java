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
public abstract class DoubleGaugeData implements Data<DoublePointData> {
  public static DoubleGaugeData create(Collection<DoublePointData> points) {
    return new AutoValue_DoubleGaugeData(points);
  }

  DoubleGaugeData() {}

  @Override
  public abstract Collection<DoublePointData> getPoints();
}
