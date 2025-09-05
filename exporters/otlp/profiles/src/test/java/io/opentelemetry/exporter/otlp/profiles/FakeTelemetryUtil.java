/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileDictionaryData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableValueTypeData;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.ByteBuffer;
import java.util.Collections;

// TODO eventually merge with io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil
class FakeTelemetryUtil {

  private static final ProfileDictionaryData EMPTY_PROFILE_DICTIONARY_DATA =
      ImmutableProfileDictionaryData.create(
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyList());

  private FakeTelemetryUtil() {}

  private static final InstrumentationScopeInfo SCOPE_INFO =
      InstrumentationScopeInfo.builder("testLib")
          .setVersion("1.0")
          .setSchemaUrl("http://url")
          .build();

  /** Generate a fake {@link ProfileData}. */
  static ProfileData generateFakeProfileData() {
    String profileId = "0123456789abcdef0123456789abcdef";
    return ImmutableProfileData.create(
        Resource.create(Attributes.empty()),
        SCOPE_INFO,
        EMPTY_PROFILE_DICTIONARY_DATA,
        ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE),
        Collections.emptyList(),
        5L,
        6L,
        ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE),
        7L,
        Collections.emptyList(),
        profileId,
        Collections.emptyList(),
        3,
        "format",
        ByteBuffer.wrap(new byte[] {4, 5}));
  }
}
