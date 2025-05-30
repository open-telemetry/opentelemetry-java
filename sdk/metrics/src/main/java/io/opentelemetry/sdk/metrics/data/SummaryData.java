/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#SUMMARY} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface SummaryData extends Data<SummaryPointData> {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static SummaryData create(Collection<SummaryPointData> points) {
    return ImmutableSummaryData.create(points);
  }
}
