/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.api.common.Attributes;

/**
 * ResourceProvider is a service provider for additional {@link Resource}s. Users of OpenTelemetry
 * SDK can use it to add custom {@link Resource} attributes.
 *
 * <p>Fully qualified class name of the implementation should be registered in {@code
 * META-INF/services/io.opentelemetry.sdk.resources.ResourceProvider}.
 *
 * <p>Resources specified via system properties or environment variables will take precedence over
 * any value supplied via {@code ResourceProvider}.
 */
public abstract class ResourceProvider {

  public Resource create() {
    return Resource.create(getAttributes());
  }

  protected abstract Attributes getAttributes();
}
