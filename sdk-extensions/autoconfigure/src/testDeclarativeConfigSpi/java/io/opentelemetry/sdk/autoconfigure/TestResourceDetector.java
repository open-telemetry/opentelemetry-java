/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.resources.Resource;

public class TestResourceDetector implements ComponentProvider, AutoConfigureListener {

  @SuppressWarnings("NonFinalStaticField")
  static boolean initialized = false;

  @Override
  public void afterAutoConfigure(OpenTelemetrySdk sdk) {
    initialized = true;
  }

  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    return Resource.empty();
  }
}
