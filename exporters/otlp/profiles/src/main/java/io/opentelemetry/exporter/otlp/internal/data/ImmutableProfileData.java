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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.ByteBuffer;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ProfileData}, which represents a complete profile, including
 * sample types, samples, mappings to binaries, locations, functions, string table, and additional
 * metadata.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableProfileData implements ProfileData {

  /**
   * Returns a new ProfileData representing the given data.
   *
   * @return a new ProfileData representing the given data.
   */
  @SuppressWarnings("TooManyParameters")
  public static ProfileData create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      List<ValueTypeData> sampleTypes,
      List<SampleData> samples,
      List<MappingData> mappingTable,
      List<LocationData> locationTable,
      List<Integer> locationIndices,
      List<FunctionData> functionTable,
      Attributes attributeTable,
      List<AttributeUnitData> attributeUnits,
      List<LinkData> linkTable,
      List<String> stringTable,
      long timeNanos,
      long durationNanos,
      ValueTypeData periodType,
      long period,
      List<Integer> commentStrindices,
      int defaultSampleTypeStringIndex,
      String profileId,
      Attributes attributes,
      int droppedAttributesCount,
      String originalPayloadFormat,
      ByteBuffer originalPayload) {
    return new AutoValue_ImmutableProfileData(
        resource,
        instrumentationScopeInfo,
        sampleTypes,
        samples,
        mappingTable,
        locationTable,
        locationIndices,
        functionTable,
        attributeTable,
        attributeUnits,
        linkTable,
        stringTable,
        timeNanos,
        durationNanos,
        periodType,
        period,
        commentStrindices,
        defaultSampleTypeStringIndex,
        profileId,
        attributes,
        droppedAttributesCount,
        originalPayloadFormat,
        originalPayload);
  }

  ImmutableProfileData() {}
}
