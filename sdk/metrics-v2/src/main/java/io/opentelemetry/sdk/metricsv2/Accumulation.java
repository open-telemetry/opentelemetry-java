/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metricsv2.data.MetricData;

interface Accumulation {
  long getStartTime();

  MetricData.Point convertToPoint(long epochNanos, Labels labels);

  MetricData.Type getMetricDataType();
}
