/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import javax.annotation.concurrent.Immutable;

/**
 * Data for a {@link MetricDataType#SUMMARY} metric.
 *
 * @since 1.14.0
 */
@Immutable
public interface SummaryData extends Data<SummaryPointData> {}
