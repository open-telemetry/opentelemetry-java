/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for storing metrics by {@link MetricDescriptor} and returning access to input
 * pipeline for instrument measurements.
 *
 * <p>Each descriptor in the registry results in an exported metric stream. Under normal
 * circumstances each descriptor shares a unique {@link MetricDescriptor#getName()}. When multiple
 * descriptors share the same name, an identity conflict has occurred. The registry detects identity
 * conflicts on {@link #register(MetricStorage)} and logs diagnostic information when they occur.
 * See {@link MetricDescriptor#isCompatibleWith(MetricDescriptor)} for definition of compatibility.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class MetricStorageRegistry {
  private static final Logger logger = Logger.getLogger(MetricStorageRegistry.class.getName());

  private final Object lock = new Object();

  @GuardedBy("lock")
  private final Map<MetricDescriptor, MetricStorage> registry = new HashMap<>();

  /** Returns a {@link Collection} of the registered {@link MetricStorage}. */
  public Collection<MetricStorage> getStorages() {
    synchronized (lock) {
      return Collections.unmodifiableCollection(new ArrayList<>(registry.values()));
    }
  }

  /**
   * Registers the metric {@code newStorage} to this registry. If a metric with compatible identity
   * was previously registered, returns the previously registered storage. If a metric with the same
   * name (case-insensitive) but incompatible {@link MetricDescriptor} was previously registered,
   * logs a diagnostic warning and returns the {@code newStorage}.
   *
   * @param newStorage the metric storage to use or discard.
   * @return the {@code newStorage} if no compatible metric is already registered, otherwise the
   *     previously registered storage.
   */
  @SuppressWarnings("unchecked")
  public <I extends MetricStorage> I register(I newStorage) {
    MetricDescriptor descriptor = newStorage.getMetricDescriptor();
    I oldOrNewStorage;
    List<MetricStorage> storages;
    synchronized (lock) {
      oldOrNewStorage = (I) registry.computeIfAbsent(descriptor, key -> newStorage);
      // If storage was NOT added to the registry, its description was a perfect match to one
      // previously registered and we can skip detecting identity conflicts
      if (newStorage != oldOrNewStorage || !logger.isLoggable(Level.WARNING)) {
        return oldOrNewStorage;
      }
      storages = new ArrayList<>(registry.values());
    }
    // Else, we need to look for identity conflicts with previously registered storages
    for (MetricStorage storage : storages) {
      // Skip the newly registered storage
      if (storage == newStorage) {
        continue;
      }
      MetricDescriptor existing = storage.getMetricDescriptor();
      // Check compatibility of metrics which share the same case-insensitive name
      if (existing.getName().equalsIgnoreCase(descriptor.getName())
          && !existing.isCompatibleWith(descriptor)) {
        logger.log(Level.WARNING, DebugUtils.duplicateMetricErrorMessage(existing, descriptor));
        break; // Only log information about the first conflict found to reduce noise
      }
    }
    // Finally, return the storage
    return oldOrNewStorage;
  }

  /** Reset the storage registry, clearing all storages. */
  void resetForTest() {
    synchronized (lock) {
      registry.clear();
    }
  }
}
