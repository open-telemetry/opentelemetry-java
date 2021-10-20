/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class MetricDataImpl implements MetricData {

  static MetricDataImpl create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit,
      MetricDataType type,
      Data<?> data) {
    return new AutoValue_MetricDataImpl(
        resource, instrumentationLibraryInfo, name, description, unit, type, data);
  }
}
