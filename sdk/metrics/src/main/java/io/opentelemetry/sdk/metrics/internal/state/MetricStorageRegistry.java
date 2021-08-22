/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Responsible for storing metrics (by name) and returning access to input pipeline for instrument
 * wiring.
 *
 * <p>The rules of the registry:
 *
 * <ul>
 *   <li>Only one storage type may be registered per-name. Repeated look-ups per-name will return
 *       the same storage.
 *   <li>The metric descriptor should be "compatible", when returning an existing metric storage,
 *       i.e. same type of metric, same name, description etc.
 *   <li>The registered storage type MUST be either always Asynchronous or always Synchronous. No
 *       mixing and matching.
 * </ul>
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class MetricStorageRegistry {
  // TODO: Maybe we store metrics *and* instrument interfaces separately here...
  private final ConcurrentMap<String, MetricStorage> registry = new ConcurrentHashMap<>();

  /**
   * Returns a {@code Collection} view of the registered {@link MetricStorage}.
   *
   * @return a {@code Collection} view of the registered {@link MetricStorage}.
   */
  public Collection<MetricStorage> getMetrics() {
    return Collections.unmodifiableCollection(new ArrayList<>(registry.values()));
  }

  /**
   * Registers the given {@code Metric} to this registry. Returns the registered storage if no other
   * metric with the same name is registered or a previously registered metric with same name and
   * equal with the current metric, otherwise throws an exception.
   *
   * @param storage the metric storage to use or discard.
   * @return the given metric storage if no metric with same name already registered, otherwise the
   *     previous registered instrument.
   * @throws IllegalArgumentException if instrument cannot be registered.
   */
  @SuppressWarnings("unchecked")
  public <I extends MetricStorage> I register(I storage) {
    MetricDescriptor descriptor = storage.getMetricDescriptor();
    MetricStorage oldOrNewStorage =
        registry.computeIfAbsent(descriptor.getName().toLowerCase(), key -> storage);
    // Make sure the storage is compatible.
    if (!oldOrNewStorage.getMetricDescriptor().isCompatibleWith(descriptor)) {
      throw new IllegalArgumentException(
          "Metric with same name and different descriptor already created.   Found: "
              + oldOrNewStorage.getMetricDescriptor()
              + ", Want: "
              + descriptor);
    }
    // Make sure we aren't mixing sync + async.
    if (!storage.getClass().equals(oldOrNewStorage.getClass())) {
      throw new IllegalArgumentException(
          "Metric with same name and different instrument already created.   Found: "
              + oldOrNewStorage.getClass()
              + ", Want: "
              + storage.getClass());
    }

    return (I) oldOrNewStorage;
  }
}
