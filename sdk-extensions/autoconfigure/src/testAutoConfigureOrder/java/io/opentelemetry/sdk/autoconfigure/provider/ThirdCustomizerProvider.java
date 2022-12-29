/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import static java.util.Collections.singletonMap;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

public class ThirdCustomizerProvider implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addPropertiesSupplier(
        () -> singletonMap("otel.from-config", "configured value"));
  }

  @Override
  public int order() {
    return 3000;
  }
}
