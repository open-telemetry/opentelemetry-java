/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.spi;

import io.opentelemetry.api.OpenTelemetry;

/**
 * A service provider interface (SPI) for a {@link OpenTelemetry}. Fully qualified class name of the
 * implementation should be registered in a resource file {@code
 * META-INF/services/io.opentelemetry.spi.OpenTelemetryFactory}.
 *
 * <p>A specific implementation can be selected by setting the system property {@code
 * io.opentelemetry.spi.OpenTelemetryFactory} with the value of the fully qualified class name.
 *
 * @deprecated Use {@link io.opentelemetry.api.DefaultOpenTelemetry#builder} to initialize
 *     OpenTelemetry with a custom provider, or {@code OpenTelemetrySdk#builder} or {@code
 *     opentelemetry-sdk-extension-autoconfigure} to configure the default SDK.
 */
@Deprecated
public interface OpenTelemetryFactory {

  /** Returns a new {@link OpenTelemetry} instance. */
  OpenTelemetry create();
}
