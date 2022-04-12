/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.List;

/**
 * Records values from asynchronous instruments to associated {@link AsynchronousMetricStorage}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SdkObservableMeasurement
    implements ObservableLongMeasurement, ObservableDoubleMeasurement {

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final InstrumentDescriptor instrumentDescriptor;
  private final List<AsynchronousMetricStorage<?>> storages;

  private SdkObservableMeasurement(
      InstrumentationScopeInfo instrumentationScopeInfo,
      InstrumentDescriptor instrumentDescriptor,
      List<AsynchronousMetricStorage<?>> storages) {
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.instrumentDescriptor = instrumentDescriptor;
    this.storages = storages;
  }

  /**
   * Create a {@link SdkObservableMeasurement}.
   *
   * @param instrumentationScopeInfo the instrumentation scope info of corresponding meter
   * @param instrumentDescriptor the instrument descriptor
   * @param storages the storages to record to
   * @return the observable measurement
   */
  public static SdkObservableMeasurement create(
      InstrumentationScopeInfo instrumentationScopeInfo,
      InstrumentDescriptor instrumentDescriptor,
      List<AsynchronousMetricStorage<?>> storages) {
    return new SdkObservableMeasurement(instrumentationScopeInfo, instrumentDescriptor, storages);
  }

  /** Get the instrumentation scope info. */
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  InstrumentDescriptor getInstrumentDescriptor() {
    return instrumentDescriptor;
  }

  List<AsynchronousMetricStorage<?>> getStorages() {
    return storages;
  }

  @Override
  public void record(long value) {
    record(value, Attributes.empty());
  }

  @Override
  public void record(long value, Attributes attributes) {
    for (AsynchronousMetricStorage<?> storage : storages) {
      storage.recordLong(value, attributes);
    }
  }

  @Override
  public void record(double value) {
    record(value, Attributes.empty());
  }

  @Override
  public void record(double value, Attributes attributes) {
    for (AsynchronousMetricStorage<?> storage : storages) {
      storage.recordDouble(value, attributes);
    }
  }
}
