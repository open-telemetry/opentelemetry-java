/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ResourceDetector;
import java.util.Optional;

public class TestColorResourceDetector implements ResourceDetector<String> {

  @Override
  public Optional<String> readData(ConfigProperties config) {
    return Optional.of("blue");
  }

  @Override
  public void registerAttributes(Builder<String> builder) {
    builder.add(AttributeKey.stringKey("color"), Optional::of);
  }

  @Override
  public String name() {
    return "color";
  }
}
