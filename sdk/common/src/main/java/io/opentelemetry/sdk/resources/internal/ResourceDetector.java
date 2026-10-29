/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;

/** A source of a {@link Resource} to be merged by an {@link SdkResourceProvider}. */
interface ResourceDetector {

  // TODO(jack-berg): Add shouldReinvoke() or equivalent to distinguish static detectors from
  // detectors whose entity
  // descriptive attributes can change at runtime.

  /**
   * Returns a diagnostic label for logging / debugging, or {@code null} if unnamed. This is not a
   * detector lookup identifier and need not be unique.
   */
  @Nullable
  String getName();

  /** Returns the {@link Resource} detected by this detector. */
  Resource getResource();
}
