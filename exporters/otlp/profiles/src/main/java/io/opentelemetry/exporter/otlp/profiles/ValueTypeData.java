/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * ValueType describes the type and units of a value, with an optional aggregation temporality.
 *
 * @see "profiles.proto::ValueType"
 */
@Immutable
public interface ValueTypeData {

  /** Index into string table. */
  int getTypeStringIndex();

  /** Index into string table. */
  int getUnitStringIndex();

  @Nullable
  AggregationTemporality getAggregationTemporality();
}
