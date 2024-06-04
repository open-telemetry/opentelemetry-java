/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.profiles.ProfileContainerData;
import io.opentelemetry.exporter.otlp.profiles.ProfileData;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableProfileContainerData implements ProfileContainerData {

  @SuppressWarnings("TooManyParameters")
  public static ProfileContainerData create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      String profileId,
      long startEpochNanos,
      long endEpochNanos,
      Attributes attributes,
      int totalAttributeCount,
      @Nullable String originalPayloadFormat,
      ByteBuffer originalPayload,
      ProfileData profile) {
    return new AutoValue_ImmutableProfileContainerData(
        resource,
        instrumentationScopeInfo,
        profileId,
        startEpochNanos,
        endEpochNanos,
        attributes,
        totalAttributeCount,
        originalPayloadFormat,
        originalPayload,
        profile);
  }

  ImmutableProfileContainerData() {}

  public ByteBuffer getOriginalPayloadReadOnly() {
    return getOriginalPayload().asReadOnlyBuffer();
  }
}
