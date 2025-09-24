/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.SampleData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link SampleData}, which records values encountered in some program
 * context.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableSampleData implements SampleData {

  /**
   * Returns a new SampleData representing the given program context.
   *
   * @return a new SampleData representing the given program context.
   */
  public static SampleData create(
      int stackIndex,
      List<Long> values,
      List<Integer> attributeIndices,
      int linkIndex,
      List<Long> timestamps) {
    return new AutoValue_ImmutableSampleData(
        stackIndex, values, attributeIndices, linkIndex, timestamps);
  }

  ImmutableSampleData() {}
}
