/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.ProfileData;
import io.opentelemetry.exporter.otlp.profiles.ProfileDictionaryData;
import io.opentelemetry.exporter.otlp.profiles.SampleData;
import io.opentelemetry.exporter.otlp.profiles.ValueTypeData;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.ByteBuffer;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ProfileData}, which represents a complete profile, including
 * sample types, samples, mappings to binaries, locations, and additional metadata.
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
      ProfileDictionaryData profileDictionaryData,
      List<ValueTypeData> sampleTypes,
      List<SampleData> samples,
      List<Integer> locationIndices,
      long timeNanos,
      long durationNanos,
      ValueTypeData periodType,
      long period,
      List<Integer> commentStrIndices,
      int defaultSampleTypeStringIndex,
      String profileId,
      List<Integer> attributeIndices,
      int droppedAttributesCount,
      String originalPayloadFormat,
      ByteBuffer originalPayload) {
    return new AutoValue_ImmutableProfileData(
        resource,
        instrumentationScopeInfo,
        profileDictionaryData,
        sampleTypes,
        samples,
        locationIndices,
        timeNanos,
        durationNanos,
        periodType,
        period,
        commentStrIndices,
        defaultSampleTypeStringIndex,
        profileId,
        attributeIndices,
        droppedAttributesCount,
        originalPayloadFormat,
        originalPayload);
  }

  ImmutableProfileData() {}
}
