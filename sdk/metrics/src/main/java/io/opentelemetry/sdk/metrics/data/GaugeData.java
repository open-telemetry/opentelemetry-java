/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#LONG_GAUGE} or {@link MetricDataType#DOUBLE_GAUGE} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface GaugeData<T extends PointData> extends Data<T> {}
