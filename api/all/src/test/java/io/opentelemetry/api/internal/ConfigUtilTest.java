/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

/** Relies on environment configuration in {@code ./api/all/build.gradle.kts}. */
class ConfigUtilTest {

  @Test
  @SetSystemProperty(key = "config.key", value = "system")
  void getString_SystemPropertyPriority() {
    assertThat(ConfigUtil.getString("config.key", "default")).isEqualTo("system");
    assertThat(ConfigUtil.getString("config-key", "default")).isEqualTo("system");
    assertThat(ConfigUtil.getString("other.config.key", "default")).isEqualTo("default");
  }

  @Test
  @SetSystemProperty(key = "CONFIG-KEY", value = "system")
  void getString_SystemPropertyNormalized() {
    assertThat(ConfigUtil.getString("config.key", "default")).isEqualTo("system");
    assertThat(ConfigUtil.getString("config-key", "default")).isEqualTo("system");
    assertThat(ConfigUtil.getString("other.config.key", "default")).isEqualTo("default");
  }

  @Test
  void getString_EnvironmentVariable() {
    assertThat(ConfigUtil.getString("config.key", "default")).isEqualTo("environment");
    assertThat(ConfigUtil.getString("other.config.key", "default")).isEqualTo("default");
  }

  @Test
  void normalizeEnvironmentVariable() {
    assertThat(ConfigUtil.normalizeEnvironmentVariableKey("CONFIG_KEY")).isEqualTo("config.key");
    assertThat(ConfigUtil.normalizeEnvironmentVariableKey("config_key")).isEqualTo("config.key");
    assertThat(ConfigUtil.normalizeEnvironmentVariableKey("config-key")).isEqualTo("config-key");
    assertThat(ConfigUtil.normalizeEnvironmentVariableKey("configkey")).isEqualTo("configkey");
  }

  @Test
  void normalizePropertyKey() {
    assertThat(ConfigUtil.normalizePropertyKey("CONFIG_KEY")).isEqualTo("config_key");
    assertThat(ConfigUtil.normalizePropertyKey("CONFIG.KEY")).isEqualTo("config.key");
    assertThat(ConfigUtil.normalizePropertyKey("config-key")).isEqualTo("config.key");
    assertThat(ConfigUtil.normalizePropertyKey("configkey")).isEqualTo("configkey");
  }

  @Test
  void defaultIfnull() {
    assertThat(ConfigUtil.defaultIfNull("val1", "val2")).isEqualTo("val1");
    assertThat(ConfigUtil.defaultIfNull(null, "val2")).isEqualTo("val2");
  }

  @Test
  @SuppressWarnings("ReturnValueIgnored")
  void systemPropertiesConcurrentAccess() throws ExecutionException, InterruptedException {
    int threads = 4;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    try {
      int cycles = 1000;
      CountDownLatch latch = new CountDownLatch(1);
      List<Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        futures.add(
            executor.submit(
                () -> {
                  try {
                    latch.await();
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                  }

                  for (int j = 0; j < cycles; j++) {
                    String property = "prop " + j;
                    System.setProperty(property, "a");
                    System.getProperties().remove(property);
                  }
                }));
      }

      latch.countDown();
      for (int i = 0; i < cycles; i++) {
        assertThatCode(() -> ConfigUtil.getString("x", "y")).doesNotThrowAnyException();
      }

      for (Future<?> future : futures) {
        future.get();
      }

    } finally {
      executor.shutdownNow();
    }
  }
}
