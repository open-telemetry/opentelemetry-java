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
public abstract class DoubleSummaryData implements Data<DoubleSummaryPoint> {
  DoubleSummaryData() {}

  public static DoubleSummaryData create(Collection<DoubleSummaryPoint> points) {
    return new AutoValue_DoubleSummaryData(points);
  }

  @Override
  public abstract Collection<DoubleSummaryPoint> getPoints();
}
