/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.incubator.config.ConfigChangeListener;
import io.opentelemetry.api.incubator.config.ConfigChangeRegistration;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SDK implementation of {@link ConfigProvider}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SdkConfigProvider implements ConfigProvider {
  private static final Logger logger = Logger.getLogger(SdkConfigProvider.class.getName());
  private static final ConfigChangeRegistration NOOP_CHANGE_REGISTRATION = () -> {};

  private final Object lock = new Object();
  private volatile DeclarativeConfigProperties openTelemetryConfigModel;
  private final ConcurrentMap<String, CopyOnWriteArrayList<ListenerRegistration>> listenersByPath =
      new ConcurrentHashMap<>();
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  private SdkConfigProvider(DeclarativeConfigProperties openTelemetryConfigModel) {
    this.openTelemetryConfigModel = requireNonNull(openTelemetryConfigModel);
  }

  /**
   * Create a {@link SdkConfigProvider}.
   *
   * @param openTelemetryConfigModel {@link DeclarativeConfigProperties} corresponding to the {@code
   *     OpenTelemetryConfiguration} type, i.e. the root node.
   * @return the {@link SdkConfigProvider} instance
   */
  public static SdkConfigProvider create(DeclarativeConfigProperties openTelemetryConfigModel) {
    return new SdkConfigProvider(openTelemetryConfigModel);
  }

  @Override
  public DeclarativeConfigProperties getInstrumentationConfig() {
    return openTelemetryConfigModel.get("instrumentation/development");
  }

  @Override
  public ConfigChangeRegistration addConfigChangeListener(
      String path, ConfigChangeListener listener) {
    requireNonNull(listener, "listener");
    String watchedPath = normalizeAndValidatePath(path);
    if (isShutdown.get()) {
      return NOOP_CHANGE_REGISTRATION;
    }
    synchronized (lock) {
      if (isShutdown.get()) {
        return NOOP_CHANGE_REGISTRATION;
      }
      ListenerRegistration registration = new ListenerRegistration(watchedPath, listener);
      listenersByPath
          .computeIfAbsent(watchedPath, unused -> new CopyOnWriteArrayList<>())
          .add(registration);
      return registration;
    }
  }

  /**
   * Sets the configuration value at the given path.
   *
   * <p>The path uses {@code .} as a separator. The final segment is the key to set within the
   * parent mapping. For example, {@code
   * setConfig(".instrumentation/development.java.myLib.enabled", true)} sets the {@code enabled}
   * key within the {@code .instrumentation/development.java.myLib} mapping.
   *
   * <p>The value may be a String, Boolean, Long, Double, Integer, {@link
   * DeclarativeConfigProperties}, or a List whose elements are any of those types.
   *
   * <p>If a value already exists at the path, its type must not change.
   *
   * @param path the full declarative configuration path, including the key to set
   * @param value the new value
   * @throws IllegalArgumentException if the path does not include a key segment beyond the root, or
   *     if the value type is not supported
   * @throws DeclarativeConfigException if the path traverses a non-mapping value, or if the
   *     existing value's type would change
   */
  public void setConfig(String path, Object value) {
    requireNonNull(value, "value");
    validateValue(value);
    Object normalizedValue = normalizeValue(value);
    String normalizedPath = normalizeAndValidatePath(path);
    int lastDot = normalizedPath.lastIndexOf('.');
    String parentPath = lastDot == 0 ? "." : normalizedPath.substring(0, lastDot);
    String key = normalizedPath.substring(lastDot + 1);
    if (key.isEmpty()) {
      throw new IllegalArgumentException(
          "setConfig path must include a key segment beyond the root: " + path);
    }
    if (isShutdown.get()) {
      return;
    }
    synchronized (lock) {
      DeclarativeConfigProperties current = openTelemetryConfigModel;
      Map<String, Object> currentRootMap = DeclarativeConfigProperties.toMap(current);
      validateTypeUnchanged(currentRootMap, parentPath, key, normalizedValue, normalizedPath);
      Map<String, Object> newRootMap =
          withValueAtPath(currentRootMap, parentPath, key, normalizedValue, normalizedPath);
      openTelemetryConfigModel =
          YamlDeclarativeConfigProperties.create(newRootMap, current.getComponentLoader());
      notifyListeners(current, openTelemetryConfigModel);
    }
  }

  private void notifyListeners(
      DeclarativeConfigProperties previous, DeclarativeConfigProperties updated) {
    if (isShutdown.get()) {
      return;
    }

    for (Map.Entry<String, CopyOnWriteArrayList<ListenerRegistration>> entry :
        listenersByPath.entrySet()) {
      String watchedPath = entry.getKey();
      DeclarativeConfigProperties previousConfigAtPath = resolvePath(previous, watchedPath);
      DeclarativeConfigProperties updatedConfigAtPath = resolvePath(updated, watchedPath);
      if (hasSameContents(previousConfigAtPath, updatedConfigAtPath)) {
        continue;
      }

      for (ListenerRegistration registration : entry.getValue()) {
        registration.notifyChange(watchedPath, updatedConfigAtPath);
      }
    }
  }

  void shutdown() {
    synchronized (lock) {
      if (!isShutdown.compareAndSet(false, true)) {
        return;
      }
      listenersByPath.clear();
    }
  }

  /**
   * Returns a new map with {@code key}={@code value} set within the map at {@code parentPath}.
   * Intermediate maps along the path are copied, not mutated.
   */
  private static Map<String, Object> withValueAtPath(
      Map<String, Object> rootMap, String parentPath, String key, Object value, String fullPath) {
    String relativePath = parentPath.substring(1);
    if (relativePath.isEmpty()) {
      Map<String, Object> copy = new HashMap<>(rootMap);
      copy.put(key, value);
      return copy;
    }
    // TODO: this can be done in a single traversal rather than resolving the leaf then replacing
    String[] segments = relativePath.split("\\.");
    Map<String, Object> leafMap = resolveToMap(rootMap, segments, fullPath);
    Map<String, Object> updatedLeaf = new HashMap<>(leafMap);
    updatedLeaf.put(key, value);
    return copyAndReplace(rootMap, segments, 0, fullPath, updatedLeaf);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> copyAndReplace(
      Map<String, Object> current,
      String[] segments,
      int depth,
      String normalizedPath,
      Map<String, Object> replacement) {
    Map<String, Object> copy = new HashMap<>(current);
    String segment = segments[depth];
    if (depth == segments.length - 1) {
      copy.put(segment, replacement);
      return copy;
    }
    Object child = current.get(segment);
    Map<String, Object> childMap;
    if (child instanceof Map) {
      childMap = (Map<String, Object>) child;
    } else if (child == null) {
      childMap = new HashMap<>();
    } else {
      throw schemaConflict(normalizedPath, segment, child);
    }
    copy.put(segment, copyAndReplace(childMap, segments, depth + 1, normalizedPath, replacement));
    return copy;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> resolveToMap(
      Map<String, Object> rootMap, String[] segments, String normalizedPath) {
    Map<String, Object> current = rootMap;
    for (String segment : segments) {
      Object child = current.get(segment);
      if (child instanceof Map) {
        current = (Map<String, Object>) child;
      } else if (child == null) {
        return new HashMap<>();
      } else {
        throw schemaConflict(normalizedPath, segment, child);
      }
    }
    return current;
  }

  private static DeclarativeConfigException schemaConflict(
      String normalizedPath, String segment, Object actual) {
    return new DeclarativeConfigException(
        "Cannot traverse path '"
            + normalizedPath
            + "': segment '"
            + segment
            + "' resolves to a "
            + typeName(actual)
            + ", not a mapping");
  }

  // TODO: optimize later, this is an expensive operation.
  // But note that we only do this on a mutation, and these are expected to be infrquent
  // so maybe acceptable
  private static boolean hasSameContents(
      DeclarativeConfigProperties left, DeclarativeConfigProperties right) {
    return DeclarativeConfigProperties.toMap(left).equals(DeclarativeConfigProperties.toMap(right));
  }

  private static DeclarativeConfigProperties resolvePath(
      DeclarativeConfigProperties root, String watchedPath) {
    String relativePath = watchedPath.substring(1);
    if (relativePath.isEmpty()) {
      return root;
    }

    DeclarativeConfigProperties current = root;
    String[] segments = relativePath.split("\\.");
    for (String segment : segments) {
      if (segment.isEmpty()) {
        return DeclarativeConfigProperties.empty();
      }
      current = current.get(segment);
    }
    return current;
  }

  private static void validateTypeUnchanged(
      Map<String, Object> rootMap,
      String parentPath,
      String key,
      Object newValue,
      String fullPath) {
    String relativePath = parentPath.substring(1);
    Map<String, Object> leafMap =
        relativePath.isEmpty()
            ? rootMap
            : resolveToMap(rootMap, relativePath.split("\\."), fullPath);
    Object existing = leafMap.get(key);
    if (existing == null) {
      return; // key doesn't exist yet, any type is allowed
    }
    boolean typeMismatch;
    if (existing instanceof Map) {
      typeMismatch = !(newValue instanceof Map);
    } else if (existing instanceof List) {
      typeMismatch = !(newValue instanceof List);
    } else {
      typeMismatch = !existing.getClass().equals(newValue.getClass());
    }
    if (typeMismatch) {
      throw new DeclarativeConfigException(
          "Cannot change type at path '"
              + fullPath
              + "' from "
              + typeName(existing)
              + " to "
              + typeName(newValue));
    }
  }

  private static void validateValue(Object value) {
    if (value instanceof String
        || value instanceof Boolean
        || value instanceof Long
        || value instanceof Double
        || value instanceof Integer
        || value instanceof DeclarativeConfigProperties) {
      return;
    }
    if (value instanceof List) {
      for (Object element : (List<?>) value) {
        if (!(element instanceof String)
            && !(element instanceof Boolean)
            && !(element instanceof Long)
            && !(element instanceof Double)
            && !(element instanceof Integer)
            && !(element instanceof DeclarativeConfigProperties)) {
          throw new IllegalArgumentException(
              "setConfig list value elements must be String, Boolean, Long, Double, Integer, or"
                  + " DeclarativeConfigProperties, got: "
                  + (element == null ? "null" : element.getClass().getName()));
        }
      }
      return;
    }
    throw new IllegalArgumentException(
        "setConfig value must be a String, Boolean, Long, Double, Integer,"
            + " DeclarativeConfigProperties, or List thereof, got: "
            + value.getClass().getName());
  }

  private static Object normalizeValue(Object value) {
    if (value instanceof DeclarativeConfigProperties) {
      return DeclarativeConfigProperties.toMap((DeclarativeConfigProperties) value);
    }
    if (!(value instanceof List)) {
      return value;
    }
    List<Object> normalized = new ArrayList<>();
    for (Object element : (List<?>) value) {
      normalized.add(
          element instanceof DeclarativeConfigProperties
              ? DeclarativeConfigProperties.toMap((DeclarativeConfigProperties) element)
              : element);
    }
    return normalized;
  }

  private static String typeName(Object value) {
    if (value instanceof Map) {
      return "mapping";
    }
    if (value instanceof List) {
      return "list";
    }
    return value.getClass().getSimpleName();
  }

  private static String normalizeAndValidatePath(String path) {
    String watchedPath = requireNonNull(path, "path").trim();
    if (!watchedPath.startsWith(".")) {
      throw new IllegalArgumentException("Path must be absolute and start with '.': " + path);
    }
    if (watchedPath.indexOf('*') >= 0) {
      throw new IllegalArgumentException("Path does not support wildcards: " + path);
    }
    if (watchedPath.indexOf('[') >= 0 || watchedPath.indexOf(']') >= 0) {
      throw new IllegalArgumentException("Path does not support sequence indexing: " + path);
    }
    return watchedPath;
  }

  private final class ListenerRegistration implements ConfigChangeRegistration {
    private final String watchedPath;
    private final ConfigChangeListener listener;

    private ListenerRegistration(String watchedPath, ConfigChangeListener listener) {
      this.watchedPath = watchedPath;
      this.listener = listener;
    }

    @Override
    public void close() {
      synchronized (lock) {
        CopyOnWriteArrayList<ListenerRegistration> registrations = listenersByPath.get(watchedPath);
        if (registrations == null) {
          return;
        }
        registrations.remove(this);
        if (registrations.isEmpty()) {
          listenersByPath.remove(watchedPath, registrations);
        }
      }
    }

    private void notifyChange(String changedPath, DeclarativeConfigProperties updatedConfigAtPath) {
      try {
        listener.onChange(changedPath, updatedConfigAtPath);
      } catch (Throwable throwable) {
        logger.log(
            Level.WARNING,
            "Config change listener threw while handling path " + changedPath,
            throwable);
      }
    }
  }

  @Override
  public String toString() {
    return "SdkConfigProvider{" + "instrumentationConfig=" + getInstrumentationConfig() + '}';
  }
}
