/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongGauge;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;

class SdkLongGauge extends AbstractInstrument implements LongGauge {

  final SdkMeter sdkMeter;
  final WriteableMetricStorage storage;

  SdkLongGauge(InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor);
    this.sdkMeter = sdkMeter;
    this.storage = storage;
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
  }

  @Override
  public void set(long value, Attributes attributes) {
    storage.recordLong(value, attributes, Context.current());
  }

  @Override
  public void set(long value, Attributes attributes, Context context) {
    storage.recordLong(value, attributes, context);
  }

  @Override
  public void set(long value) {
    set(value, Attributes.empty());
  }

  @Override
  public LongGauge bind(Attributes attributes) {
    return storage.cachedBind(attributes, BoundLongGauge::new);
  }

  static class SdkLongGaugeBuilder implements LongGaugeBuilder {

    final InstrumentBuilder builder;

    SdkLongGaugeBuilder(
        SdkMeter sdkMeter,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      builder =
          new InstrumentBuilder(name, InstrumentType.GAUGE, InstrumentValueType.LONG, sdkMeter)
              .setDescription(description)
              .setUnit(unit)
              .setAdviceBuilder(adviceBuilder);
    }

    @Override
    public LongGaugeBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public LongGaugeBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public SdkLongGauge build() {
      return builder.buildSynchronousInstrument(SdkLongGauge::new);
    }

    @Override
    public ObservableLongGauge buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      return builder.buildLongAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }

  private final class BoundLongGauge implements LongGauge {

    private final AggregatorHandle<?> op;
    private final Attributes boundAttributes;

    BoundLongGauge(AggregatorHandle<?> op, Attributes boundAttributes) {
      this.op = op;
      this.boundAttributes = boundAttributes;
    }

    @Override
    public void set(long value) {
      op.recordLong(value);
    }

    @Override
    public void set(long value, Attributes attributes) {
      SdkLongGauge.this.set(value, boundAttributes.toBuilder().putAll(attributes).build());
    }

    @Override
    public void set(long value, Attributes attributes, Context context) {
      SdkLongGauge.this.set(value, boundAttributes.toBuilder().putAll(attributes).build(), context);
    }

    @Override
    public LongGauge bind(Attributes attributes) {
      return SdkLongGauge.this.bind(boundAttributes.toBuilder().putAll(attributes).build());
    }

    @Override
    public boolean isEnabled() {
      return SdkLongGauge.this.isEnabled();
    }
  }
}
