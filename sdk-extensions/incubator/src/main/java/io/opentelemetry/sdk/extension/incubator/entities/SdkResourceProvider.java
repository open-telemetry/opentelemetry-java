/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.Resource;
import io.opentelemetry.api.incubator.entities.ResourceProvider;

/** The SDK implementation of {@link ResourceProvider}. */
public final class SdkResourceProvider implements ResourceProvider {
  private final SdkResource resource = new SdkResource();

  @Override
  public Resource getResource() {
    return resource;
  }

  public io.opentelemetry.sdk.resources.Resource getSdkResource() {
    return resource.getResource();
  }

  public static SdkResourceProviderBuilder builder() {
    return new SdkResourceProviderBuilder();
  }
}
