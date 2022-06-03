/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Point data with a {@code double} aggregation value.
 *
 * @since 1.14.0
 */
@Immutable
public interface DoublePointData extends PointData {
  /** Returns the value of the data point. */
  double getValue();

  /** List of exemplars collected from measurements aggregated into this point. */
  @Override
  List<DoubleExemplarData> getExemplars();
}
