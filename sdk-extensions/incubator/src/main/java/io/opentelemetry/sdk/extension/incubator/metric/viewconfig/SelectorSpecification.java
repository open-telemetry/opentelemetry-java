/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.InstrumentType;
import javax.annotation.Nullable;

@AutoValue
abstract class SelectorSpecification {

  static AutoValue_SelectorSpecification.Builder builder() {
    return new AutoValue_SelectorSpecification.Builder();
  }

  @Nullable
  abstract String getInstrumentName();

  @Nullable
  abstract InstrumentType getInstrumentType();

  @Nullable
  abstract String getInstrumentUnit();

  @Nullable
  abstract String getMeterName();

  @Nullable
  abstract String getMeterVersion();

  @Nullable
  abstract String getMeterSchemaUrl();

  @AutoValue.Builder
  interface Builder {
    Builder instrumentName(@Nullable String instrumentName);

    Builder instrumentType(@Nullable InstrumentType instrumentType);

    Builder instrumentUnit(@Nullable String instrumentUnit);

    Builder meterName(@Nullable String meterName);

    Builder meterVersion(@Nullable String meterVersion);

    Builder meterSchemaUrl(@Nullable String meterSchemaUrl);

    SelectorSpecification build();
  }
}
