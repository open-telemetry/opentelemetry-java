/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class IncubatingUtilTest {

  @Test
  void classNotFoundException() {
    assertThatCode(
            () ->
                IncubatingUtil.createWithFactory(
                    "test",
                    () -> {
                      Class.forName("foo");
                      return null;
                    }))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "Error configuring from test. Is opentelemetry-sdk-extension-incubator on the classpath?");
  }

  @Test
  void invocationTargetException() {
    assertThatCode(
            () ->
                IncubatingUtil.createWithFactory(
                    "test",
                    () -> {
                      throw new InvocationTargetException(new RuntimeException("test exception"));
                    }))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unexpected error configuring from test")
        .hasRootCauseMessage("test exception");
  }
}
