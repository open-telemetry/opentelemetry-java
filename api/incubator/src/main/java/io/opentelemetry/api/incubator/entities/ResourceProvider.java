/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

/**
 * A registry for interacting with {@link Resource}s. The name <i>Provider</i> is for consistency
 * with other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see Resource
 */
public interface ResourceProvider {
  /**
   * Returns a no-op {@link ResourceProvider} which only creates no-op {@link Resource}s which do
   * not record nor are emitted.
   */
  static ResourceProvider noop() {
    return new NoopResourceProvider();
  }

  /** Returns the active {@link Resource} for which Telemetry is reported. */
  Resource getResource();
}
