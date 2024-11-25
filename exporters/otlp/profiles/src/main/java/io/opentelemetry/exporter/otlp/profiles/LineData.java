/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import javax.annotation.concurrent.Immutable;

/**
 * Details a specific line in a source code, linked to a function.
 *
 * @see "profiles.proto::Line"
 */
@Immutable
public interface LineData {

  /** The index of the corresponding Function for this line. Index into function table. */
  int getFunctionIndex();

  /** Line number in source code. */
  long getLine();

  /** Column number in source code. */
  long getColumn();
}
