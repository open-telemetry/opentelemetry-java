/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.Immutable;

/**
 * Describes a metric that will be output.
 *
 * <p>Provides equality/identity semantics for detecting duplicate metrics of incompatible.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class MetricDescriptor {

  private final AtomicReference<SourceInfo> viewSourceInfo = new AtomicReference<>();
  private int hashcode;

  /**
   * Constructs a metric descriptor with no instrument and default view.
   *
   * <p>Used for testing + empty-storage only.
   */
  public static MetricDescriptor create(String name, String description, String unit) {
    return create(
        View.builder().build(),
        SourceInfo.fromCurrentStack(),
        InstrumentDescriptor.create(
            name,
            description,
            unit,
            InstrumentType.OBSERVABLE_GAUGE,
            InstrumentValueType.DOUBLE,
            Advice.empty()));
  }

  /** Constructs a metric descriptor for a given View + instrument. */
  public static MetricDescriptor create(
      View view, SourceInfo viewSourceInfo, InstrumentDescriptor instrument) {
    String name = (view.getName() == null) ? instrument.getName() : view.getName();
    String description =
        (view.getDescription() == null) ? instrument.getDescription() : view.getDescription();
    MetricDescriptor metricDescriptor =
        new AutoValue_MetricDescriptor(name, description, view, instrument);
    metricDescriptor.viewSourceInfo.set(viewSourceInfo);
    return metricDescriptor;
  }

  MetricDescriptor() {}

  /**
   * The name of the descriptor, equal to {@link View#getName()} if not null, else {@link
   * InstrumentDescriptor#getName()}.
   */
  public abstract String getName();

  /**
   * The description of the descriptor, equal to {@link View#getDescription()} if not null, else
   * {@link InstrumentDescriptor#getDescription()}.
   */
  public abstract String getDescription();

  /** The view that lead to the creation of this metric. */
  public abstract View getView();

  /**
   * The {@link SourceInfo} from where the view was registered. Ignored from {@link #equals(Object)}
   * and {@link #toString()}.
   */
  public final SourceInfo getViewSourceInfo() {
    SourceInfo sourceInfo = viewSourceInfo.get();
    return sourceInfo == null ? SourceInfo.noSourceInfo() : sourceInfo;
  }

  /** The instrument which lead to the creation of this metric. */
  public abstract InstrumentDescriptor getSourceInstrument();

  /** The {@link AggregationUtil#aggregationName(Aggregation)} of the view aggregation. */
  public String getAggregationName() {
    return AggregationUtil.aggregationName(getView().getAggregation());
  }

  /** Uses case-insensitive version of {@link #getName()}. */
  @Override
  public final int hashCode() {
    int result = hashcode;
    if (result == 0) {
      result = 1;
      result *= 1000003;
      result ^= getName().toLowerCase(Locale.ROOT).hashCode();
      result *= 1000003;
      result ^= getDescription().hashCode();
      result *= 1000003;
      result ^= getView().hashCode();
      result *= 1000003;
      result ^= getSourceInstrument().hashCode();
      hashcode = result;
    }
    return result;
  }

  /** Uses case-insensitive version of {@link #getName()}. */
  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MetricDescriptor) {
      MetricDescriptor that = (MetricDescriptor) o;
      return this.getName().equalsIgnoreCase(that.getName())
          && this.getDescription().equals(that.getDescription())
          && this.getView().equals(that.getView())
          && this.getSourceInstrument().equals(that.getSourceInstrument());
    }
    return false;
  }
}
