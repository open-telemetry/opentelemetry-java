/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.BuildIdKind;
import io.opentelemetry.exporter.otlp.profiles.MappingData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableMappingData implements MappingData {

  @SuppressWarnings("TooManyParameters")
  public static MappingData create(
      long memoryStart,
      long memoryLimit,
      long fileOffset,
      long filenameIndex,
      long buildIdIndex,
      BuildIdKind buildIdKind,
      List<Long> attributeIndices,
      boolean hasFunctions,
      boolean hasFilenames,
      boolean hasLineNumbers,
      boolean hasInlineFrames) {
    return new AutoValue_ImmutableMappingData(
        memoryStart,
        memoryLimit,
        fileOffset,
        filenameIndex,
        buildIdIndex,
        buildIdKind,
        attributeIndices,
        hasFunctions,
        hasFilenames,
        hasLineNumbers,
        hasInlineFrames);
  }

  ImmutableMappingData() {}
}
