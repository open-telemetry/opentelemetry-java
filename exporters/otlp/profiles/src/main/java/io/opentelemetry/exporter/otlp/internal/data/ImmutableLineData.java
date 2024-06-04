/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.LineData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableLineData implements LineData {

  public static LineData create(long functionIndex, long line, long column) {
    return new AutoValue_ImmutableLineData(functionIndex, line, column);
  }

  ImmutableLineData() {}
}
