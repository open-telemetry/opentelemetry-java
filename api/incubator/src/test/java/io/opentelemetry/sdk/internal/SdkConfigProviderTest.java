/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.ConfigChangeRegistration;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class SdkConfigProviderTest {

  @Test
  void addConfigChangeListener_notifiesOnWatchedPathChange() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    List<String> notifications = new ArrayList<>();
    ConfigChangeRegistration registration =
        provider.addConfigChangeListener(
            ".instrumentation/development.general.http",
            (path, newConfig) -> notifications.add(path + "=" + newConfig.getString("enabled")));

    provider.updateOpenTelemetryConfigModel(
        config(
            mapOf(
                "instrumentation/development",
                mapOf("general", mapOf("http", mapOf("enabled", "true"))))));

    assertThat(notifications).containsExactly(".instrumentation/development.general.http=true");
    registration.close();
  }

  @Test
  void addConfigChangeListener_ignoresUnchangedAndNonWatchedUpdates() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf(
                        "general",
                        mapOf("http", mapOf("enabled", "true")),
                        "java",
                        mapOf("servlet", mapOf("enabled", "true"))))));
    AtomicInteger callbackCount = new AtomicInteger();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> callbackCount.incrementAndGet());

    provider.updateOpenTelemetryConfigModel(
        config(
            mapOf(
                "instrumentation/development",
                mapOf(
                    "general",
                    mapOf("http", mapOf("enabled", "true")),
                    "java",
                    mapOf("servlet", mapOf("enabled", "false"))))));
    provider.updateOpenTelemetryConfigModel(
        config(
            mapOf(
                "instrumentation/development",
                mapOf(
                    "general",
                    mapOf("http", mapOf("enabled", "true")),
                    "java",
                    mapOf("servlet", mapOf("enabled", "false"))))));

    assertThat(callbackCount).hasValue(0);
  }

  @Test
  void addConfigChangeListener_returnsEmptyNodeWhenWatchedPathCleared() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "true"))))));
    List<Set<String>> propertyKeysSeen = new ArrayList<>();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> propertyKeysSeen.add(newConfig.getPropertyKeys()));

    provider.updateOpenTelemetryConfigModel(
        config(mapOf("instrumentation/development", mapOf("general", mapOf()))));

    assertThat(propertyKeysSeen).containsExactly(Collections.emptySet());
  }

  @Test
  void addConfigChangeListener_closeAndShutdownStopCallbacks() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    AtomicInteger callbackCount = new AtomicInteger();
    ConfigChangeRegistration registration =
        provider.addConfigChangeListener(
            ".instrumentation/development.general.http",
            (path, newConfig) -> callbackCount.incrementAndGet());

    registration.close();
    registration.close();
    provider.updateOpenTelemetryConfigModel(
        config(
            mapOf(
                "instrumentation/development",
                mapOf("general", mapOf("http", mapOf("enabled", "true"))))));
    assertThat(callbackCount).hasValue(0);

    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> callbackCount.incrementAndGet());
    provider.shutdown();
    provider.updateOpenTelemetryConfigModel(
        config(
            mapOf(
                "instrumentation/development",
                mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    assertThat(callbackCount).hasValue(0);
  }

  @Test
  void addConfigChangeListener_listenerExceptionIsIsolated() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    AtomicInteger successfulCallbacks = new AtomicInteger();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> {
          throw new IllegalStateException("boom");
        });
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> successfulCallbacks.incrementAndGet());

    provider.updateOpenTelemetryConfigModel(
        config(
            mapOf(
                "instrumentation/development",
                mapOf("general", mapOf("http", mapOf("enabled", "true"))))));

    assertThat(successfulCallbacks).hasValue(1);
  }

  @Test
  void updateConfig_replacesSubtreeAndNotifiesListener() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    List<String> notifications = new ArrayList<>();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> notifications.add(path + "=" + newConfig.getString("enabled")));

    provider.updateConfig(
        ".instrumentation/development.general.http", config(mapOf("enabled", "true")));

    assertThat(notifications).containsExactly(".instrumentation/development.general.http=true");
  }

  @Test
  void updateConfig_doesNotNotifyWhenSubtreeUnchanged() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "true"))))));
    AtomicInteger callbackCount = new AtomicInteger();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> callbackCount.incrementAndGet());

    provider.updateConfig(
        ".instrumentation/development.general.http", config(mapOf("enabled", "true")));

    assertThat(callbackCount).hasValue(0);
  }

  @Test
  void updateConfig_createsIntermediateNodesIfMissing() {
    SdkConfigProvider provider = SdkConfigProvider.create(config(mapOf()));
    List<String> notifications = new ArrayList<>();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> notifications.add(path + "=" + newConfig.getString("enabled")));

    provider.updateConfig(
        ".instrumentation/development.general.http", config(mapOf("enabled", "true")));

    assertThat(notifications).containsExactly(".instrumentation/development.general.http=true");
  }

  @Test
  void updateConfig_noopWhenDisposed() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    AtomicInteger callbackCount = new AtomicInteger();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> callbackCount.incrementAndGet());
    provider.shutdown();

    provider.updateConfig(
        ".instrumentation/development.general.http", config(mapOf("enabled", "true")));

    assertThat(callbackCount).hasValue(0);
  }

  @Test
  void setConfigProperty_setsScalarAndNotifiesListener() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    List<String> notifications = new ArrayList<>();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> notifications.add(path + "=" + newConfig.getString("enabled")));

    provider.setConfigProperty(".instrumentation/development.general.http", "enabled", "true");

    assertThat(notifications).containsExactly(".instrumentation/development.general.http=true");
  }

  @Test
  void setConfigProperty_doesNotNotifyWhenValueUnchanged() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "true"))))));
    AtomicInteger callbackCount = new AtomicInteger();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> callbackCount.incrementAndGet());

    provider.setConfigProperty(".instrumentation/development.general.http", "enabled", "true");

    assertThat(callbackCount).hasValue(0);
  }

  @Test
  void setConfigProperty_noopWhenDisposed() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    AtomicInteger callbackCount = new AtomicInteger();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> callbackCount.incrementAndGet());
    provider.shutdown();

    provider.setConfigProperty(".instrumentation/development.general.http", "enabled", "true");

    assertThat(callbackCount).hasValue(0);
  }

  @Test
  void concurrentUpdates_allChangesAreApplied() throws Exception {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("count", "0"))))));
    List<String> notifications = new CopyOnWriteArrayList<>();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> notifications.add(newConfig.getString("count")));

    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      int index = i + 1;
      futures.add(
          executor.submit(
              () -> {
                try {
                  startLatch.await();
                  provider.setConfigProperty(
                      ".instrumentation/development.general.http", "count", String.valueOf(index));
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                } finally {
                  doneLatch.countDown();
                }
              }));
    }
    startLatch.countDown();
    assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
    for (java.util.concurrent.Future<?> future : futures) {
      future.get(1, TimeUnit.SECONDS);
    }
    executor.shutdown();

    assertThat(notifications).hasSize(threadCount);
    DeclarativeConfigProperties finalConfig =
        provider.getInstrumentationConfig().get("general").get("http");
    assertThat(finalConfig.getString("count")).isNotNull();
  }

  @Test
  void pathValidation_rejectsMissingLeadingDot() {
    SdkConfigProvider provider = SdkConfigProvider.create(config(mapOf()));

    assertThatThrownBy(
            () -> provider.addConfigChangeListener("instrumentation", (path, newConfig) -> {}))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> provider.updateConfig("instrumentation", config(mapOf())))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> provider.setConfigProperty("instrumentation", "key", "value"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void pathValidation_rejectsWildcards() {
    SdkConfigProvider provider = SdkConfigProvider.create(config(mapOf()));

    assertThatThrownBy(() -> provider.addConfigChangeListener(".*", (path, newConfig) -> {}))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> provider.updateConfig(".foo.*", config(mapOf())))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> provider.setConfigProperty(".foo.*", "key", "value"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void pathValidation_rejectsBrackets() {
    SdkConfigProvider provider = SdkConfigProvider.create(config(mapOf()));

    assertThatThrownBy(
            () -> provider.addConfigChangeListener(".foo[0]", (path, newConfig) -> {}))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> provider.updateConfig(".foo[0]", config(mapOf())))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> provider.setConfigProperty(".foo[0]", "key", "value"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateConfig_rootPathReplacesEntireConfig() {
    SdkConfigProvider provider =
        SdkConfigProvider.create(
            config(
                mapOf(
                    "instrumentation/development",
                    mapOf("general", mapOf("http", mapOf("enabled", "false"))))));
    List<String> notifications = new ArrayList<>();
    provider.addConfigChangeListener(
        ".instrumentation/development.general.http",
        (path, newConfig) -> notifications.add(path + "=" + newConfig.getString("enabled")));

    provider.updateConfig(
        ".",
        config(
            mapOf(
                "instrumentation/development",
                mapOf("general", mapOf("http", mapOf("enabled", "true"))))));

    assertThat(notifications).containsExactly(".instrumentation/development.general.http=true");
  }

  private static DeclarativeConfigProperties config(Map<String, Object> root) {
    return new MapBackedDeclarativeConfigProperties(root);
  }

  private static Map<String, Object> mapOf(Object... entries) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (int i = 0; i < entries.length; i += 2) {
      result.put((String) entries[i], entries[i + 1]);
    }
    return result;
  }

  private static final class MapBackedDeclarativeConfigProperties
      implements DeclarativeConfigProperties {
    private static final ComponentLoader COMPONENT_LOADER =
        ComponentLoader.forClassLoader(MapBackedDeclarativeConfigProperties.class.getClassLoader());

    private final Map<String, Object> values;

    private MapBackedDeclarativeConfigProperties(Map<String, Object> values) {
      this.values = values;
    }

    @Override
    public String getString(String name) {
      Object value = values.get(name);
      return value instanceof String ? (String) value : null;
    }

    @Override
    public Boolean getBoolean(String name) {
      Object value = values.get(name);
      return value instanceof Boolean ? (Boolean) value : null;
    }

    @Override
    public Integer getInt(String name) {
      Object value = values.get(name);
      return value instanceof Integer ? (Integer) value : null;
    }

    @Override
    public Long getLong(String name) {
      Object value = values.get(name);
      if (value instanceof Long) {
        return (Long) value;
      }
      if (value instanceof Integer) {
        return ((Integer) value).longValue();
      }
      return null;
    }

    @Override
    public Double getDouble(String name) {
      Object value = values.get(name);
      if (value instanceof Double) {
        return (Double) value;
      }
      if (value instanceof Number) {
        return ((Number) value).doubleValue();
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> List<T> getScalarList(String name, Class<T> scalarType) {
      Object value = values.get(name);
      if (!(value instanceof List)) {
        return null;
      }
      List<Object> raw = (List<Object>) value;
      List<T> casted = new ArrayList<>(raw.size());
      for (Object element : raw) {
        if (!scalarType.isInstance(element)) {
          return null;
        }
        casted.add((T) element);
      }
      return casted;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DeclarativeConfigProperties getStructured(String name) {
      Object value = values.get(name);
      if (!(value instanceof Map)) {
        return null;
      }
      return new MapBackedDeclarativeConfigProperties((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public List<DeclarativeConfigProperties> getStructuredList(String name) {
      Object value = values.get(name);
      if (!(value instanceof List)) {
        return null;
      }
      List<Object> raw = (List<Object>) value;
      List<DeclarativeConfigProperties> result = new ArrayList<>(raw.size());
      for (Object element : raw) {
        if (!(element instanceof Map)) {
          return null;
        }
        result.add(new MapBackedDeclarativeConfigProperties((Map<String, Object>) element));
      }
      return result;
    }

    @Override
    public Set<String> getPropertyKeys() {
      return values.keySet();
    }

    @Override
    public ComponentLoader getComponentLoader() {
      return COMPONENT_LOADER;
    }
  }
}
