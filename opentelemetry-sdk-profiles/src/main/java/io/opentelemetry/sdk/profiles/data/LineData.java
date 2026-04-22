/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import javax.annotation.concurrent.Immutable;

/**
 * Details a specific line in a source code, linked to a function.
 *
 * @see "profiles.proto::Line"
 */
@Immutable
public interface LineData {

  /**
   * Returns a new LineData describing the given details a specific line in a source code.
   *
   * @return a new LineData describing the given details a specific line in a source code.
   */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static LineData create(int functionIndex, long line, long column) {
    return new AutoValue_ImmutableLineData(functionIndex, line, column);
  }

  /** The index of the corresponding Function for this line. Index into function table. */
  int getFunctionIndex();

  /** Line number in source code. */
  long getLine();

  /** Column number in source code. */
  long getColumn();
}
