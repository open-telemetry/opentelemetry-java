/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.LineData;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link LineData}, which details a specific line in a source code,
 * linked to a function.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableLineData implements LineData {

  /**
   * Returns a new LineData describing the given details a specific line in a source code.
   *
   * @return a new LineData describing the given details a specific line in a source code.
   */
  public static LineData create(int functionIndex, long line, long column) {
    return new AutoValue_ImmutableLineData(functionIndex, line, column);
  }

  ImmutableLineData() {}
}
