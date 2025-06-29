/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.resources.Resource;

/**
 * A provider of {@link Resource} for this SDK.
 *
 * <p>{@code EntityProvider} is responsible for:
 *
 * <p>- Detecting Entities using registered detectors. - Providing thread-safe access to the current
 * resource.
 *
 * <p>The future of this class may include the ability to add/remove {@link Entity} objects or
 * re-run detectors.
 */
public interface EntityProvider {
  /** the current {@link Resource} detected. */
  public Resource getResource();

  /** A builder of {@link EntityProvider}. */
  public static EntityProviderBuilder builder() {
    return new SdkEntityProviderBuilder();
  }
}
