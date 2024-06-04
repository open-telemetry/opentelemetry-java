/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.AggregationTemporality;
import io.opentelemetry.exporter.otlp.profiles.ValueTypeData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableValueTypeData implements ValueTypeData {

  public static ValueTypeData create(
      long type, long unit, AggregationTemporality aggregationTemporality) {
    return new AutoValue_ImmutableValueTypeData(type, unit, aggregationTemporality);
  }

  ImmutableValueTypeData() {}
}
