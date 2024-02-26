/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ResourceDetector;
import java.util.Optional;

public class TestServiceNameResourceDetector implements ResourceDetector<String> {

  @Override
  public Optional<String> readData(ConfigProperties config) {
    return Optional.of("cart");
  }

  @Override
  public void registerAttributes(Builder<String> builder) {
    builder.add(AttributeKey.stringKey("service.name"), Optional::of);
  }

  @Override
  public String name() {
    return "name";
  }
}
