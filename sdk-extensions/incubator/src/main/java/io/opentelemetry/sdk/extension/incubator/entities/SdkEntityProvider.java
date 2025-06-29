/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.resources.Resource;

/**
 * Instance of {@link EntityProvider}.
 *
 * <p>This class doesn't do much now, but will expand in responsibilities.
 */
class SdkEntityProvider implements EntityProvider {
  private final Resource resource;

  SdkEntityProvider(Resource resource) {
    this.resource = resource;
  }

  @Override
  public Resource getResource() {
    return resource;
  }
}
