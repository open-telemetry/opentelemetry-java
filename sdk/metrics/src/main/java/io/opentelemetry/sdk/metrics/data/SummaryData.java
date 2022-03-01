/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.Collection;

public interface SummaryData extends Data<SummaryPointData> {
  @Override
  Collection<SummaryPointData> getPoints();
}
