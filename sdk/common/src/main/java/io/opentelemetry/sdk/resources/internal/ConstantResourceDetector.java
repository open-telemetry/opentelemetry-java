/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;

final class ConstantResourceDetector implements ResourceDetector {

  @Nullable private final String name;
  private final Resource resource;

  ConstantResourceDetector(@Nullable String name, Resource resource) {
    this.name = name;
    this.resource = resource;
  }

  @Nullable
  @Override
  public String getName() {
    return name;
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  @Override
  public boolean shouldReinvoke() {
    return false;
  }

  @Override
  public String toString() {
    return "ConstantResourceDetector{name=" + name + ", resource=" + resource + "}";
  }
}
