/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Describes the mapping of a binary in memory.
 *
 * @see "profiles.proto::Mapping"
 */
@Immutable
public interface MappingData {

  /** Address at which the binary (or DLL) is loaded into memory. */
  long getMemoryStart();

  /** The limit of the address range occupied by this mapping. */
  long getMemoryLimit();

  /** Offset in the binary that corresponds to the first mapped address. */
  long getFileOffset();

  /**
   * The object this entry is loaded from. This can be a filename on disk for the main binary and
   * shared libraries, or virtual abstraction like "[vdso]". Index into the string table.
   */
  int getFilenameStringIndex();

  /** References to attributes in Profile.attribute_table. */
  List<Integer> getAttributeIndices();
}
