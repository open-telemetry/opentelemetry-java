/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

/**
 * A summary metric.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#summary
 */
public interface SummaryData extends Data<SummaryPointData> {}
