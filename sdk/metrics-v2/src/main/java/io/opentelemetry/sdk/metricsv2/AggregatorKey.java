/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class AggregatorKey {
  static AggregatorKey create(
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      Labels labels) {
    return new AutoValue_AggregatorKey(instrumentationLibraryInfo, instrumentDescriptor, labels);
  }

  abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  abstract InstrumentDescriptor getInstrumentDescriptor();

  abstract Labels getLabels();
}
