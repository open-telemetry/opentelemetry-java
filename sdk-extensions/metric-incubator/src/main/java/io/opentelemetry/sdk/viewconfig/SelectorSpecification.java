/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = AutoValue_SelectorSpecification.Builder.class)
abstract class SelectorSpecification {

  static AutoValue_SelectorSpecification.Builder builder() {
    return new AutoValue_SelectorSpecification.Builder();
  }

  @Nullable
  abstract String getInstrumentName();

  @Nullable
  abstract InstrumentType getInstrumentType();

  @Nullable
  abstract String getMeterName();

  @Nullable
  abstract String getMeterVersion();

  @Nullable
  abstract String getMeterSchemaUrl();

  @AutoValue.Builder
  interface Builder {
    @JsonProperty("instrument_name")
    Builder instrumentName(@Nullable String instrumentName);

    @JsonProperty("instrument_type")
    Builder instrumentType(@Nullable InstrumentType instrumentType);

    @JsonProperty("meter_name")
    Builder meterName(@Nullable String meterName);

    @JsonProperty("meter_version")
    Builder meterVersion(@Nullable String meterVersion);

    @JsonProperty("meter_schema_url")
    Builder meterSchemaUrl(@Nullable String meterSchemaUrl);

    SelectorSpecification build();
  }
}
