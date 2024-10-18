/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

/** A Registry which provides the {@link Resource} to the SDK. */
public final class ResourceProvider {
  private final Resource resource;

  ResourceProvider(Resource resource) {
    this.resource = resource;
  }

  /**
   * Provides the currently discovered {@link Resource}
   *
   * @return the Resource.
   */
  public final Resource getResource() {
    return resource;
  }

  /** Returns a builder for ResourceProvider. */
  public static ResourceProviderBuilder builder() {
    return new ResourceProviderBuilder();
  }
}
