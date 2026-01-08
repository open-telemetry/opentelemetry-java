/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.ValueTypeData;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ValueTypeData}, which describes the type and units of a
 * value.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableValueTypeData implements ValueTypeData {

  /**
   * Returns a new ValueTypeData describing the given type and unit characteristics.
   *
   * @return a new ValueTypeData describing the given type and unit characteristics.
   */
  public static ValueTypeData create(int typeStringIndex, int unitStringIndex) {
    return new AutoValue_ImmutableValueTypeData(typeStringIndex, unitStringIndex);
  }

  ImmutableValueTypeData() {}
}
