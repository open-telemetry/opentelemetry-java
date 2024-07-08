/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.LineData;
import io.opentelemetry.exporter.otlp.profiles.LocationData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link LocationData}, which describes function and line table debug
 * information.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableLocationData implements LocationData {

  /**
   * Returns a new LocationData describing the given function and line table information.
   *
   * @return a new LocationData describing the given function and line table information.
   */
  public static LocationData create(
      long mappingIndex,
      long address,
      List<LineData> lines,
      boolean folded,
      int typeIndex,
      List<Long> attributes) {
    return new AutoValue_ImmutableLocationData(
        mappingIndex, address, lines, folded, typeIndex, attributes);
  }

  ImmutableLocationData() {}
}
