/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.profiles.AttributeUnitData;
import io.opentelemetry.exporter.otlp.profiles.FunctionData;
import io.opentelemetry.exporter.otlp.profiles.LinkData;
import io.opentelemetry.exporter.otlp.profiles.LocationData;
import io.opentelemetry.exporter.otlp.profiles.MappingData;
import io.opentelemetry.exporter.otlp.profiles.ProfileData;
import io.opentelemetry.exporter.otlp.profiles.SampleData;
import io.opentelemetry.exporter.otlp.profiles.ValueTypeData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableProfileData implements ProfileData {

  @SuppressWarnings("TooManyParameters")
  public static ProfileData create(
      List<ValueTypeData> sampleTypes,
      List<SampleData> samples,
      List<MappingData> mappings,
      List<LocationData> locations,
      List<Long> locationIndices,
      List<FunctionData> functions,
      Attributes attributes,
      List<AttributeUnitData> attributeUnits,
      List<LinkData> links,
      List<String> stringTable,
      long dropFrames,
      long keepFrames,
      long timeNanos,
      long durationNanos,
      ValueTypeData periodType,
      long period,
      List<Long> comment,
      long defaultSampleType) {
    return new AutoValue_ImmutableProfileData(
        sampleTypes,
        samples,
        mappings,
        locations,
        locationIndices,
        functions,
        attributes,
        attributeUnits,
        links,
        stringTable,
        dropFrames,
        keepFrames,
        timeNanos,
        durationNanos,
        periodType,
        period,
        comment,
        defaultSampleType);
  }

  ImmutableProfileData() {}
}
