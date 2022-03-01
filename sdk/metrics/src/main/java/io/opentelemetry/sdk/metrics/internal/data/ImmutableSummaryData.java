/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
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
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time
 */
@Immutable
@AutoValue
public abstract class ImmutableSummaryData implements SummaryData {

  private static final ImmutableSummaryData EMPTY =
      ImmutableSummaryData.create(Collections.emptyList());

  public static ImmutableSummaryData empty() {
    return EMPTY;
  }

  ImmutableSummaryData() {}

  public static ImmutableSummaryData create(Collection<SummaryPointData> points) {
    return new AutoValue_ImmutableSummaryData(points);
  }
}
