/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import javax.annotation.concurrent.Immutable;

/**
 * Details a specific line in a source code, linked to a function.
 *
 * @see "pprofextended.proto::Line"
 */
@Immutable
public interface LineData {

  /** The index of the corresponding Function for this line. Index into function table. */
  long getFunctionIndex();

  /** Line number in source code. */
  long getLine();

  /** Column number in source code. */
  long getColumn();
}
