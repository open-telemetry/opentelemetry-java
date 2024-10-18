/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

public final class ResourceProvider {
  private final Resource resource;

  ResourceProvider(Resource resource) {
    this.resource = resource;
  }

  public final Resource getResource() {
    return resource;
  }

  public static ResourceProviderBuilder builder() {
    return new ResourceProviderBuilder();
  }
}
