/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class AutoConfiguredOpenTelemetrySdkAccessTest {

  @Test
  void classNotFoundException() {
    assertThatCode(
            () ->
                AutoConfiguredOpenTelemetrySdkAccess.createWithFactory(
                    () -> {
                      Class.forName("foo");
                      return null;
                    }))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "Error configuring from file. Is opentelemetry-sdk-extension-incubator on the classpath?");
  }

  @Test
  void invocationTargetException() {
    assertThatCode(
            () ->
                AutoConfiguredOpenTelemetrySdkAccess.createWithFactory(
                    () -> {
                      throw new InvocationTargetException(new RuntimeException("test exception"));
                    }))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unexpected error configuring from file")
        .hasRootCauseMessage("test exception");
  }
}
