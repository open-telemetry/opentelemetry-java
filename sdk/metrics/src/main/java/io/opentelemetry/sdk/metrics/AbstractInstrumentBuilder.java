/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Helper to make implementing builders easier. */
abstract class AbstractInstrumentBuilder<BuilderT extends AbstractInstrumentBuilder<?>> {

  static final String DEFAULT_UNIT = "1";

  private static final Logger logger = Logger.getLogger(AbstractInstrumentBuilder.class.getName());

  private final MeterProviderSharedState meterProviderSharedState;
  private String description;
  private String unit;

  protected final MeterSharedState meterSharedState;
  protected final String instrumentName;

  AbstractInstrumentBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    this.instrumentName = name;
    this.description = description;
    this.unit = unit;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
  }

  protected abstract BuilderT getThis();

  public BuilderT setUnit(String unit) {
    this.unit = unit;
    return getThis();
  }

  public BuilderT setDescription(String description) {
    this.description = description;
    return getThis();
  }

  private InstrumentDescriptor makeDescriptor(InstrumentType type, InstrumentValueType valueType) {
    String validUnit = this.unit;
    if (!isValidUnit(this.unit)) {
      validUnit = DEFAULT_UNIT;
      logger.log(
          Level.WARNING,
          "Instrument "
              + instrumentName
              + " unit \""
              + this.unit
              + "\" is invalid, using "
              + DEFAULT_UNIT
              + " instead. Instrument unit must be 63 or less ASCII characters.");
    }
    return InstrumentDescriptor.create(instrumentName, description, validUnit, type, valueType);
  }

  /**
   * Determine if the instrument unit is valid. Units must be non-null, non-blank, strings of 63 or
   * less ASCII characters.
   */
  // Visible for testing
  static boolean isValidUnit(String unit) {
    return unit != null
        && !unit.equals("")
        && unit.length() < 64
        && StandardCharsets.US_ASCII.newEncoder().canEncode(unit);
  }

  protected <T> T swapBuilder(SwapBuilder<T> swapper) {
    return swapper.newBuilder(
        meterProviderSharedState, meterSharedState, instrumentName, description, unit);
  }

  final <I extends AbstractInstrument> I buildSynchronousInstrument(
      InstrumentType type,
      InstrumentValueType valueType,
      BiFunction<InstrumentDescriptor, WriteableMetricStorage, I> instrumentFactory) {
    InstrumentDescriptor descriptor = makeDescriptor(type, valueType);
    WriteableMetricStorage storage =
        meterSharedState.registerSynchronousMetricStorage(descriptor, meterProviderSharedState);
    return instrumentFactory.apply(descriptor, storage);
  }

  final CallbackRegistration<ObservableDoubleMeasurement> registerDoubleAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableDoubleMeasurement> updater) {
    InstrumentDescriptor descriptor = makeDescriptor(type, InstrumentValueType.DOUBLE);
    return meterSharedState.registerDoubleAsynchronousInstrument(
        descriptor, meterProviderSharedState, updater);
  }

  final CallbackRegistration<ObservableLongMeasurement> registerLongAsynchronousInstrument(
      InstrumentType type, Consumer<ObservableLongMeasurement> updater) {
    InstrumentDescriptor descriptor = makeDescriptor(type, InstrumentValueType.LONG);
    return meterSharedState.registerLongAsynchronousInstrument(
        descriptor, meterProviderSharedState, updater);
  }

  @FunctionalInterface
  protected interface SwapBuilder<T> {
    T newBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name,
        String description,
        String unit);
  }
}
