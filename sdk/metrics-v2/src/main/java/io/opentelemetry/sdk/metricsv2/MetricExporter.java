/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metricsv2.data.MetricData;
import java.util.Collection;

public interface MetricExporter {
  CompletableResultCode export(Collection<MetricData> dataToExport);
}
