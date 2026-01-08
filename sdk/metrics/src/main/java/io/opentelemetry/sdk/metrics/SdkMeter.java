/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static java.util.stream.Collectors.toMap;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.state.AsynchronousMetricStorage;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorageRegistry;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.SynchronousMetricStorage;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
final class SdkMeter implements Meter {

  private static final Logger logger = Logger.getLogger(SdkMeter.class.getName());
  private static final boolean INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName("io.opentelemetry.api.incubator.metrics.ExtendedDefaultMeterProvider");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  /**
   * Instrument names MUST conform to the following syntax.
   *
   * <ul>
   *   <li>They are not null or empty strings.
   *   <li>They are case-insensitive, ASCII strings.
   *   <li>The first character must be an alphabetic character.
   *   <li>Subsequent characters must belong to the alphanumeric characters, '_', '.', '/', and '-'.
   *   <li>They can have a maximum length of 255 characters.
   * </ul>
   */
  private static final Pattern VALID_INSTRUMENT_NAME_PATTERN =
      Pattern.compile("([A-Za-z]){1}([A-Za-z0-9\\_\\-\\./]){0,254}");

  private static final Meter NOOP_METER = MeterProvider.noop().get("noop");
  private static final String NOOP_INSTRUMENT_NAME = "noop";

  private final Object collectLock = new Object();
  private final Object callbackLock = new Object();

  @GuardedBy("callbackLock")
  private final List<CallbackRegistration> callbackRegistrations = new ArrayList<>();

  private final MeterProviderSharedState meterProviderSharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final Map<RegisteredReader, MetricStorageRegistry> readerStorageRegistries;

  private volatile boolean meterEnabled;

  SdkMeter(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      List<RegisteredReader> registeredReaders,
      MeterConfig meterConfig) {
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.meterProviderSharedState = meterProviderSharedState;
    this.readerStorageRegistries =
        registeredReaders.stream()
            .collect(toMap(Function.identity(), unused -> new MetricStorageRegistry()));
    this.meterEnabled = meterConfig.isEnabled();
  }

  void updateMeterConfig(MeterConfig meterConfig) {
    meterEnabled = meterConfig.isEnabled();

    for (RegisteredReader registeredReader : readerStorageRegistries.keySet()) {
      Collection<MetricStorage> storages =
          Objects.requireNonNull(readerStorageRegistries.get(registeredReader)).getStorages();
      for (MetricStorage storage : storages) {
        storage.setEnabled(meterEnabled);
      }
    }
  }

  // Visible for testing
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  /** Collect all metrics for the meter. */
  Collection<MetricData> collectAll(RegisteredReader registeredReader, long epochNanos) {
    List<CallbackRegistration> currentRegisteredCallbacks;
    synchronized (callbackLock) {
      currentRegisteredCallbacks = new ArrayList<>(callbackRegistrations);
    }
    // Collections across all readers are sequential
    synchronized (collectLock) {
      // Only invoke callbacks if meter is enabled
      if (meterEnabled) {
        for (CallbackRegistration callbackRegistration : currentRegisteredCallbacks) {
          callbackRegistration.invokeCallback(
              registeredReader, meterProviderSharedState.getStartEpochNanos(), epochNanos);
        }
      }

      // Collect even if meter is disabled. Storage is responsible for managing state and returning
      // empty metric if disabled.
      Collection<MetricStorage> storages =
          Objects.requireNonNull(readerStorageRegistries.get(registeredReader)).getStorages();
      List<MetricData> result = new ArrayList<>(storages.size());
      for (MetricStorage storage : storages) {
        MetricData current =
            storage.collect(
                meterProviderSharedState.getResource(),
                getInstrumentationScopeInfo(),
                meterProviderSharedState.getStartEpochNanos(),
                epochNanos);
        // Ignore if the metric data doesn't have any data points, for example when aggregation is
        // Aggregation#drop()
        if (!current.isEmpty()) {
          result.add(current);
        }
      }
      return Collections.unmodifiableList(result);
    }
  }

  /** Reset the meter, clearing all registered callbacks and storages. */
  void resetForTest() {
    synchronized (collectLock) {
      synchronized (callbackLock) {
        callbackRegistrations.clear();
      }
      this.readerStorageRegistries.values().forEach(MetricStorageRegistry::resetForTest);
    }
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    if (!checkValidInstrumentName(name)) {
      return NOOP_METER.counterBuilder(NOOP_INSTRUMENT_NAME);
    }
    return INCUBATOR_AVAILABLE
        ? IncubatingUtil.createExtendedLongCounterBuilder(this, name)
        : new SdkLongCounter.SdkLongCounterBuilder(this, name);
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    if (!checkValidInstrumentName(name)) {
      return NOOP_METER.upDownCounterBuilder(NOOP_INSTRUMENT_NAME);
    }
    return INCUBATOR_AVAILABLE
        ? IncubatingUtil.createExtendedLongUpDownCounterBuilder(this, name)
        : new SdkLongUpDownCounter.SdkLongUpDownCounterBuilder(this, name);
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    if (!checkValidInstrumentName(name)) {
      return NOOP_METER.histogramBuilder(NOOP_INSTRUMENT_NAME);
    }
    return INCUBATOR_AVAILABLE
        ? IncubatingUtil.createExtendedDoubleHistogramBuilder(this, name)
        : new SdkDoubleHistogram.SdkDoubleHistogramBuilder(this, name);
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    if (!checkValidInstrumentName(name)) {
      return NOOP_METER.gaugeBuilder(NOOP_INSTRUMENT_NAME);
    }
    return INCUBATOR_AVAILABLE
        ? IncubatingUtil.createExtendedDoubleGaugeBuilder(this, name)
        : new SdkDoubleGauge.SdkDoubleGaugeBuilder(this, name);
  }

  @Override
  public BatchCallback batchCallback(
      Runnable callback,
      ObservableMeasurement observableMeasurement,
      ObservableMeasurement... additionalMeasurements) {
    Set<ObservableMeasurement> measurements = new HashSet<>();
    measurements.add(observableMeasurement);
    Collections.addAll(measurements, additionalMeasurements);

    List<SdkObservableMeasurement> sdkMeasurements = new ArrayList<>();
    for (ObservableMeasurement measurement : measurements) {
      if (!(measurement instanceof SdkObservableMeasurement)) {
        logger.log(
            Level.WARNING,
            "batchCallback called with instruments that were not created by the SDK.");
        continue;
      }
      SdkObservableMeasurement sdkMeasurement = (SdkObservableMeasurement) measurement;
      if (!instrumentationScopeInfo.equals(sdkMeasurement.getInstrumentationScopeInfo())) {
        logger.log(
            Level.WARNING,
            "batchCallback called with instruments that belong to a different Meter.");
        continue;
      }
      sdkMeasurements.add(sdkMeasurement);
    }

    CallbackRegistration callbackRegistration =
        CallbackRegistration.create(sdkMeasurements, callback);
    registerCallback(callbackRegistration);
    return new SdkObservableInstrument(this, callbackRegistration);
  }

  /**
   * Unregister the callback.
   *
   * <p>Callbacks are originally registered via {@link #registerCallback(CallbackRegistration)}.
   */
  void removeCallback(CallbackRegistration callbackRegistration) {
    synchronized (callbackLock) {
      this.callbackRegistrations.remove(callbackRegistration);
    }
  }

  /**
   * Register the callback.
   *
   * <p>The callback will be invoked once per collection until unregistered via {@link
   * #removeCallback(CallbackRegistration)}.
   */
  void registerCallback(CallbackRegistration callbackRegistration) {
    synchronized (callbackLock) {
      callbackRegistrations.add(callbackRegistration);
    }
  }

  /** Returns {@code true} if the {@link MeterConfig#enabled()} of the meter is {@code true}. */
  boolean isMeterEnabled() {
    return meterEnabled;
  }

  /** Registers new synchronous storage associated with a given instrument. */
  WriteableMetricStorage registerSynchronousMetricStorage(InstrumentDescriptor instrument) {

    List<SynchronousMetricStorage> registeredStorages = new ArrayList<>();
    for (Map.Entry<RegisteredReader, MetricStorageRegistry> entry :
        readerStorageRegistries.entrySet()) {
      RegisteredReader reader = entry.getKey();
      MetricStorageRegistry registry = entry.getValue();
      for (RegisteredView registeredView :
          reader.getViewRegistry().findViews(instrument, getInstrumentationScopeInfo())) {
        if (Aggregation.drop() == registeredView.getView().getAggregation()) {
          continue;
        }
        registeredStorages.add(
            registry.register(
                SynchronousMetricStorage.create(
                    reader,
                    registeredView,
                    instrument,
                    meterProviderSharedState.getExemplarFilter(),
                    meterEnabled)));
      }
    }

    if (registeredStorages.size() == 1) {
      return registeredStorages.get(0);
    }

    return new MultiWritableMetricStorage(registeredStorages);
  }

  /** Register new asynchronous storage associated with a given instrument. */
  SdkObservableMeasurement registerObservableMeasurement(
      InstrumentDescriptor instrumentDescriptor) {
    List<AsynchronousMetricStorage<?>> registeredStorages = new ArrayList<>();
    for (Map.Entry<RegisteredReader, MetricStorageRegistry> entry :
        readerStorageRegistries.entrySet()) {
      RegisteredReader reader = entry.getKey();
      MetricStorageRegistry registry = entry.getValue();
      for (RegisteredView registeredView :
          reader.getViewRegistry().findViews(instrumentDescriptor, getInstrumentationScopeInfo())) {
        if (Aggregation.drop() == registeredView.getView().getAggregation()) {
          continue;
        }
        registeredStorages.add(
            registry.register(
                AsynchronousMetricStorage.create(
                    reader, registeredView, instrumentDescriptor, meterEnabled)));
      }
    }

    return SdkObservableMeasurement.create(
        instrumentationScopeInfo, instrumentDescriptor, registeredStorages);
  }

  @Override
  public String toString() {
    return "SdkMeter{instrumentationScopeInfo=" + instrumentationScopeInfo + "}";
  }

  /** Check if the instrument name is valid. If invalid, log a warning. */
  // Visible for testing
  static boolean checkValidInstrumentName(String name) {
    if (name != null && VALID_INSTRUMENT_NAME_PATTERN.matcher(name).matches()) {
      return true;
    }
    if (logger.isLoggable(Level.WARNING)) {
      logger.log(
          Level.WARNING,
          "Instrument name \""
              + name
              + "\" is invalid, returning noop instrument. Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, /, and start with a letter.",
          new AssertionError());
    }

    return false;
  }

  private static class MultiWritableMetricStorage implements WriteableMetricStorage {
    private final List<? extends WriteableMetricStorage> storages;

    private MultiWritableMetricStorage(List<? extends WriteableMetricStorage> storages) {
      this.storages = storages;
    }

    @Override
    public void recordLong(long value, Attributes attributes, Context context) {
      for (WriteableMetricStorage storage : storages) {
        storage.recordLong(value, attributes, context);
      }
    }

    @Override
    public void recordDouble(double value, Attributes attributes, Context context) {
      for (WriteableMetricStorage storage : storages) {
        storage.recordDouble(value, attributes, context);
      }
    }

    @Override
    public boolean isEnabled() {
      for (WriteableMetricStorage storage : storages) {
        if (storage.isEnabled()) {
          return true;
        }
      }
      return false;
    }
  }
}
