/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;

/**
 * Extended version of {@link ComponentProvider} that allows access to the {@link ConfigProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExtendedComponentProvider extends ComponentProvider {

  /**
   * Configure an instance of the SDK extension component according to the {@code config}.
   *
   * <p>While this is the method called by the SDK, implementations can safely only implement {@link
   * #create(DeclarativeConfigProperties)} since the default implementation delegates to it.
   *
   * @param config the configuration provided where the component is referenced in a configuration
   *     file.
   * @param configProvider the configuration provider.
   * @return an instance the SDK extension component
   */
  Object create(DeclarativeConfigProperties config, ConfigProvider configProvider);

  @Override
  default Object create(DeclarativeConfigProperties config) {
    return create(config, ConfigProvider.noop());
  }
}
