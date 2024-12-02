/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import javax.annotation.concurrent.Immutable;

/**
 * Describes a function.
 *
 * @see "profiles.proto::Function"
 */
@Immutable
public interface FunctionData {

  /** Name of the function, in human-readable form if available. Index into string table. */
  int getNameStringIndex();

  /**
   * Name of the function, as identified by the system. For instance, it can be a C++ mangled name.
   * Index into string table.
   */
  int getSystemNameStringIndex();

  /** Source file containing the function. Index into string table. */
  int getFilenameStringIndex();

  /** Line number in source file. */
  long getStartLine();
}
