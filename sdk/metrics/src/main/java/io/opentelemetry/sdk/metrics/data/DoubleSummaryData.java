/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/**
 * A summary metric point.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#summary
 *
 * <p><i>Note: This is called "DoubleSummary" to reflect which primitives are used to record it,
 * however "Summary" is the equivalent OTLP type.</i>
 *
 * <p>Summary is considered a legacy metric type, and shouldn't be produced (by default) from
 * instruments.
 */
@Immutable
@AutoValue
public abstract class DoubleSummaryData implements Data<DoubleSummaryPointData> {

  static final DoubleSummaryData EMPTY = DoubleSummaryData.create(Collections.emptyList());

  DoubleSummaryData() {}

  public static DoubleSummaryData create(Collection<DoubleSummaryPointData> points) {
    return new AutoValue_DoubleSummaryData(points);
  }

  @Override
  public abstract Collection<DoubleSummaryPointData> getPoints();
}
