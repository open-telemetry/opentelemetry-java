/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.api.config.DeclarativeConfigProperties;

/**
 * Provides configured instances of SDK extension components.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @deprecated Use {@link
 *     io.opentelemetry.sdk.autoconfigure.spi.declarativeconfig.ComponentProvider} instead.
 */
@Deprecated
public interface ComponentProvider
    extends io.opentelemetry.sdk.autoconfigure.spi.declarativeconfig.ComponentProvider {

  /**
   * Configure an instance of the SDK extension component according to the {@code config}.
   *
   * @deprecated Implement {@link #create(DeclarativeConfigProperties)} instead.
   */
  @Deprecated
  default Object create(io.opentelemetry.api.incubator.config.DeclarativeConfigProperties config) {
    return create((DeclarativeConfigProperties) config);
  }

  @Override
  default Object create(DeclarativeConfigProperties config) {
    if (config instanceof io.opentelemetry.api.incubator.config.DeclarativeConfigProperties) {
      return create((io.opentelemetry.api.incubator.config.DeclarativeConfigProperties) config);
    }
    return create(new IncubatorDeclarativeConfigPropertiesAdapter(config));
  }
}
