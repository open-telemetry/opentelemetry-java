/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profiles.ProfileData;
import io.opentelemetry.sdk.profiles.ProfilesDictionaryData;
import io.opentelemetry.sdk.profiles.SampleData;
import io.opentelemetry.sdk.profiles.ValueTypeData;
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
      ProfilesDictionaryData profilesDictionaryData,
      ValueTypeData sampleType,
      List<SampleData> samples,
      long timeNanos,
      long durationNanos,
      ValueTypeData periodType,
      long period,
      String profileId,
      int droppedAttributesCount,
      String originalPayloadFormat,
      ByteBuffer originalPayload,
      List<Integer> attributeIndices) {
    return new AutoValue_ImmutableProfileData(
        resource,
        instrumentationScopeInfo,
        profilesDictionaryData,
        sampleType,
        samples,
        timeNanos,
        durationNanos,
        periodType,
        period,
        profileId,
        droppedAttributesCount,
        originalPayloadFormat,
        originalPayload,
        attributeIndices);
  }

  ImmutableProfileData() {}
}
