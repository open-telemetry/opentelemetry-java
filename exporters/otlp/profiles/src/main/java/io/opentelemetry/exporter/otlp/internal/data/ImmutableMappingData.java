/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.MappingData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link MappingData}, which describes the mapping of a binary in
 * memory.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableMappingData implements MappingData {

  /**
   * Returns a new MappingData describing the given mapping of a binary in memory.
   *
   * @return a new MappingData describing the given mapping of a binary in memory.
   */
  @SuppressWarnings("TooManyParameters")
  public static MappingData create(
      long memoryStart,
      long memoryLimit,
      long fileOffset,
      int filenameStringIndex,
      List<Integer> attributeIndices) {
    return new AutoValue_ImmutableMappingData(
        memoryStart, memoryLimit, fileOffset, filenameStringIndex, attributeIndices);
  }

  ImmutableMappingData() {}
}
