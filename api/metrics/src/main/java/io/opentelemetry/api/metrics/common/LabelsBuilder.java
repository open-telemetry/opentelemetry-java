/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.common;

/** A builder of {@link Labels} supporting an arbitrary number of key-value pairs. */
public interface LabelsBuilder {
  /** Create the {@link Labels} from this. */
  Labels build();

  /**
   * Puts a single label into this Builder.
   *
   * @return this Builder
   */
  LabelsBuilder put(String key, String value);
}
