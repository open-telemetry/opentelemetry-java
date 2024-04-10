/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

/**
 * Indicates the semantics of the build_id field.
 * @see "pprofextended.proto::BuildIdKind"
 */
public enum BuildIdKind {

  /**
   * Linker-generated build ID, stored in the ELF binary notes.
   */
  LINKER,

  /**
   * Build ID based on the content hash of the binary.
   */
  BINARY_HASH;
}
