/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.SampleData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableSampleData implements SampleData {

  public static SampleData create(
      long locationsStartIndex,
      long locationsLength,
      int stacktraceIdIndex,
      List<Long> values,
      List<Long> attributes,
      long link,
      List<Long> timestamps) {
    return new AutoValue_ImmutableSampleData(
        locationsStartIndex,
        locationsLength,
        stacktraceIdIndex,
        values,
        attributes,
        link,
        timestamps);
  }

  ImmutableSampleData() {}
}
