/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.StackData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link StackData}, which records a list of locations, starting from
 * the leaf frame.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableStackData implements StackData {

  /**
   * Returns a new StackData representing the given list of frames.
   *
   * @return a new StackData representing the given list of frames.
   */
  public static StackData create(List<Integer> locationIndices) {
    return new AutoValue_ImmutableStackData(locationIndices);
  }

  ImmutableStackData() {}
}
