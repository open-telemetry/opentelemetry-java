/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;

/** A source of a {@link Resource} to be merged by an {@link SdkResourceProvider}. */
interface ResourceDetector {

  /**
   * Returns the name of this detector, or {@code null} if unnamed. Names are informational (e.g.
   * for logging / debugging) and are not required to be unique.
   */
  @Nullable
  String getName();

  /** Returns the {@link Resource} detected by this detector. */
  Resource getResource();

  /**
   * Returns whether the enclosing {@link SdkResourceProvider} must re-invoke this detector on every
   * call to {@link SdkResourceProvider#getResource()}.
   *
   * <p>When {@code true}, {@link #getResource()} may be called on every {@link
   * SdkResourceProvider#getResource()}. When {@code false}, this detector's result may be resolved
   * once at {@link SdkResourceProvider} construction and cached.
   *
   * <p>This flag is part of the target API but has no effect on runtime behavior in the current
   * iteration - all detectors are cached at construction. TTL-based cache invalidation using this
   * flag is planned for a future iteration.
   */
  boolean shouldReinvoke();
}
