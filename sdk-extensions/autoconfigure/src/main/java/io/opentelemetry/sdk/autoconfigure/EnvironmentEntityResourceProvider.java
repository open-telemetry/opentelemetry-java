/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
// TODO(jack-berg): Figure out how to avoid this as new public API surface area prior to merging
public final class EnvironmentEntityResourceProvider implements ResourceProvider {
  @Override
  public Resource createResource(ConfigProperties config) {
    return EnvironmentResource.otelEntitiesResource(config);
  }
}
