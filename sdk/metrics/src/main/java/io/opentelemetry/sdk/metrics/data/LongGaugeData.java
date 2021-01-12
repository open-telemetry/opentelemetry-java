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
public abstract class LongGaugeData implements Data<LongPoint> {
  public static LongGaugeData create(Collection<LongPoint> points) {
    return new AutoValue_LongGaugeData(points);
  }

  LongGaugeData() {}

  @Override
  public abstract Collection<LongPoint> getPoints();
}
