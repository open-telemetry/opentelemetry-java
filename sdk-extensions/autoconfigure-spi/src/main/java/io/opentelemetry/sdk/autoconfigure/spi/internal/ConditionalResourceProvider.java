/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

/**
 * A resource provider that is only applied if the {@link #shouldApply(ConfigProperties, Resource)}
 * method returns {@code true}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ConditionalResourceProvider extends ResourceProvider {

  /**
   * If an implementation needs to apply only under certain conditions related to the config or the
   * existing state of the Resource being built, they can choose to override this default.
   *
   * @param config The auto configuration properties
   * @param existing The current state of the Resource being created
   * @return false to skip over this ResourceProvider, or true to use it
   */
  boolean shouldApply(ConfigProperties config, Resource existing);
}
