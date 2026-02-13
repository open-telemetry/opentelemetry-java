/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/**
 * {@link TraceFlagsBuilder} is used to construct {@link TraceFlags} instances which satisfy the
 * https://www.w3.org/TR/trace-context-2/#trace-flags specification.
 *
 * <p>This is a simple usage example:
 *
 * <pre>{@code
 * TraceFlags traceFlags = TraceFlags.builder().setSampled(true).build();
 * }</pre>
 *
 * <p>Implementation note: no new objects are created by the methods defined by this interface when
 * the default implementation, {@link ImmutableTraceFlags}, is used.
 */
public interface TraceFlagsBuilder {

  /**
   * Returns an instance of {@link TraceFlagsBuilder} which represents a {@link TraceFlags} object
   * which has the SAMPLED bit set if the argument is {@code true} and the SAMPLED bit cleared when
   * the argument is {@code false}. Other bits remain unchanged. The operation does not modify this
   * object.
   *
   * @param isSampled the new value for the SAMPLED bit
   * @return a {@link TraceFlagsBuilder} object representing the modified {@link TraceFlags}
   */
  TraceFlagsBuilder setSampled(boolean isSampled);

  /**
   * Returns an instance of {@link TraceFlagsBuilder} which represents a {@link TraceFlags} object
   * which has the RANDOM_TRACE_ID bit set if the argument is {@code true} and the RANDOM_TRACE_ID
   * bit cleared when the argument is {@code false}. Other bits remain unchanged. The operation does
   * not modify this object.
   *
   * @param isRandomTraceId the new value for the RANDOM_TRACE_ID bit
   * @return a {@link TraceFlagsBuilder} object representing the modified {@link TraceFlags}
   */
  TraceFlagsBuilder setRandomTraceId(boolean isRandomTraceId);

  /**
   * Returns {@link TraceFlags} represented by this object.
   *
   * @return a {@link TraceFlags} object with the bits set as configured
   */
  TraceFlags build();
}
