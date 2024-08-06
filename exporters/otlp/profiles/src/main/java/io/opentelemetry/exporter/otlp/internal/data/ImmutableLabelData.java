/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.LabelData;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link LabelData}, which provides additional context for a sample.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableLabelData implements LabelData {

  /**
   * Returns a new LabelData describing the given context for a sample.
   *
   * @return a new LabelData describing the given context for a sample.
   */
  public static LabelData create(long keyIndex, long strIndex, long num, long numUnitIndex) {
    return new AutoValue_ImmutableLabelData(keyIndex, strIndex, num, numUnitIndex);
  }

  ImmutableLabelData() {}
}
