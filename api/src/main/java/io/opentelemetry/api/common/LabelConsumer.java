/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

/** Convenience interface for consuming {@link Labels}. */
@FunctionalInterface
public interface LabelConsumer {
  void accept(String key, String value);
}
