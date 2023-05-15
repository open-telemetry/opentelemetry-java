/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

/**
 * A service provider interface (SPI) for providing additional attributes that can be used with the
 * View AttributeProcessor.
 */
public interface ConfigurableMetricAttributesProvider {

  /** Returns a {@link Attributes} that can be added to OpenTelemetry view AttributeProcessor */
  Attributes addCustomAttributes(ConfigProperties config);
}
