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

@Immutable
@AutoValue
public abstract class ImmutableLocationData implements LocationData {

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
