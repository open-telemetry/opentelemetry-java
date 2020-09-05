/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.opentelemetry.common.Attributes;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A text annotation with a set of attributes.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface Event {
  /**
   * Return the name of the {@code Event}.
   *
   * @return the name of the {@code Event}.
   * @since 0.1.0
   */
  String getName();

  /**
   * Return the attributes of the {@code Event}.
   *
   * @return the attributes of the {@code Event}.
   * @since 0.1.0
   */
  Attributes getAttributes();
}
