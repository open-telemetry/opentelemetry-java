/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.data;

import javax.annotation.concurrent.Immutable;

/**
 * Describes a function.
 *
 * @see "pprofextended.proto::Function"
 */
@Immutable
public interface FunctionData {

  /**
   * Unique nonzero id for the function.
   *
   * @deprecated retained only for pprof compatibility.
   */
  @Deprecated
  long getId();

  /** Name of the function, in human-readable form if available. Index into string table. */
  long getNameIndex();

  /**
   * Name of the function, as identified by the system. For instance, it can be a C++ mangled name.
   * Index into string table.
   */
  long getSystemNameIndex();

  /** Source file containing the function. Index into string table. */
  long getFilenameIndex();

  /** Line number in source file. */
  long getStartLine();
}
