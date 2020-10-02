/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.opentelemetry.common.Attributes;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A link to a {@link Span}.
 *
 * <p>Used (for example) in batching operations, where a single batch handler processes multiple
 * requests from different traces. Link can be also used to reference spans from the same trace.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface Link {
  /**
   * Returns the {@code SpanContext}.
   *
   * @return the {@code SpanContext}.
   * @since 0.1.0
   */
  SpanContext getContext();

  /**
   * Returns the set of attributes.
   *
   * @return the set of attributes.
   * @since 0.1.0
   */
  Attributes getAttributes();
}
