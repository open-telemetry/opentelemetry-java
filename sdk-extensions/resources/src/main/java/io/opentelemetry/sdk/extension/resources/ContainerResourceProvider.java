/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.sdk.autoconfigure.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

/** {@link ResourceProvider} for automatically configuring {@link ResourceProvider}. */
public class ContainerResourceProvider implements ResourceProvider {
  @Override
  public Resource createResource(ConfigProperties config) {
    return ContainerResource.get();
  }
}
