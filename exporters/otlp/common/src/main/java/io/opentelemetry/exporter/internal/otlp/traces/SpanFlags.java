/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.api.trace.TraceFlags;

/**
 * Represents the 32 bit span flags <a
 * href="https://github.com/open-telemetry/opentelemetry-proto/blob/342e1d4c3a1fe43312823ffb53bd38327f263059/opentelemetry/proto/trace/v1/trace.proto#L133">as
 * specified in the proto definition</a>.
 */
public final class SpanFlags {
  // As defined at:
  // https://github.com/open-telemetry/opentelemetry-proto/blob/342e1d4c3a1fe43312823ffb53bd38327f263059/opentelemetry/proto/trace/v1/trace.proto#L351-L352
  static final int CONTEXT_HAS_IS_REMOTE_BIT = 0x00000100;
  static final int CONTEXT_IS_REMOTE_BIT = 0x00000200;
  static final int CONTEXT_IS_REMOTE_MASK = CONTEXT_HAS_IS_REMOTE_BIT | CONTEXT_IS_REMOTE_BIT;

  private SpanFlags() {}

  /**
   * Returns the int (fixed32) representation of the {@link TraceFlags} enriched with the flags
   * indicating a remote parent.
   *
   * @param isParentRemote indicates whether the parent context is remote
   * @return the int (fixed32) representation of the {@link TraceFlags} enriched with the flags
   *     indicating a remote parent.
   */
  public static int withParentIsRemoteFlags(TraceFlags traceFlags, boolean isParentRemote) {
    byte byteRep = traceFlags.asByte();
    if (isParentRemote) {
      return (byteRep & 0xff) | CONTEXT_IS_REMOTE_MASK;
    }
    return (byteRep & 0xff) | CONTEXT_HAS_IS_REMOTE_BIT;
  }

  /**
   * Returns the int (fixed32) representation of the 4 bytes flags with the
   * has_parent_context_is_remote flag bit on.
   *
   * @return the int (fixed32) representation of the 4 bytes flags with the *
   *     has_parent_context_is_remote flag bit on.
   */
  public static int getHasParentIsRemoteMask() {
    return CONTEXT_HAS_IS_REMOTE_BIT;
  }

  /**
   * Checks whether the given flags contain information about parent context being remote or not.
   *
   * @param flags The int representation of the 32 bit span flags field defined in proto.
   * @return True, if the given flags contain information about the span's parent context being
   *     remote, otherwise, false.
   */
  public static boolean isKnownWhetherParentIsRemote(int flags) {
    return (flags & CONTEXT_HAS_IS_REMOTE_BIT) != 0;
  }

  /**
   * Returns the int (fixed32) representation of the 4 bytes flags with the
   * has_parent_context_is_remote and parent_context_is_remote flag bits on.
   *
   * @return the int (fixed32) representation of the 4 bytes flags with the
   *     has_parent_context_is_remote and parent_context_is_remote flag bits on.
   */
  public static int getParentIsRemoteMask() {
    return CONTEXT_IS_REMOTE_MASK;
  }

  /**
   * Checks whether in the given flags the parent is marked as remote.
   *
   * @param flags The int representation of the 32 bit span flags field defined in proto.
   * @return True, if the given flags contain information about the span's parent context and the
   *     parent is marked as remote, otherwise false.
   */
  public static boolean isParentRemote(int flags) {
    return (flags & CONTEXT_IS_REMOTE_MASK) == CONTEXT_IS_REMOTE_MASK;
  }

  /**
   * Returns the W3C {@link TraceFlags} (least significant 8 bits) portion from the given 32 bit
   * span flags fields.
   *
   * @param flags The int representation of the 32 bit span flags field defined in proto.
   * @return the W3C {@link TraceFlags} (least significant 8 bits) portion from the given 32 bit
   *     span flags fields.
   */
  public static TraceFlags getTraceFlags(int flags) {
    return TraceFlags.fromByte((byte) (flags & 0xff));
  }
}
