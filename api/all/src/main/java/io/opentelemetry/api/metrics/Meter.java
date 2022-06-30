/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides instruments used to record measurements which are aggregated to metrics.
 *
 * <p>Instruments are obtained through builders provided by this interface. Each builder has a
 * default measurement type (long or double) that may be changed.
 *
 * <p>Choosing an instrument can be hard, but here's a rule of thumb for selecting the right
 * instrument:
 *
 * <ul>
 *   <li>I want to <b>count</b> something (by recording a delta value):
 *       <ul>
 *         <li>If the value is monotonically increasing (the delta value is always non-negative) -
 *             use a Counter:
 *             <pre>meter.counterBuilder("my-counter").build()</pre>
 *         <li>If the value is NOT monotonically increasing (the delta value can be positive,
 *             negative, or zero)) - use an UpDownCounter:
 *             <pre>meter.upDownCounterBuilder("my-up-down-counter").build()</pre>
 *       </ul>
 *   <li>I want to <b>record</b> or <b>time</b> something, and the <b>statistics</b> about this
 *       thing are likely to be meaningful - use a Histogram:
 *       <pre>meter.histogramBuilder("my-histogram").build()</pre>
 *   <li>I want to <b>measure</b> something (by reporting an absolute value):
 *       <ul>
 *         <li>If it makes NO sense to add up the values across different sets of attributes, use an
 *             Asynchronous Gauge:
 *             <pre>
 *             meter.gaugeBuilder("my-gauge").buildWithCallback(observableMeasurement -> observableMeasurement.record(..))
 *             </pre>
 *         <li>If it makes sense to add up the values across different sets of attributes:
 *             <ul>
 *               <li>If the value is monotonically increasing - use an Asynchronous Counter:
 *                   <pre>
 *                   meter.counterBuilder("my-async-counter").buildWithCallback(observableMeasurement -> observableMeasurement.record(..))
 *                   </pre>
 *               <li>If the value is NOT monotonically increasing - use an Asynchronous
 *                   UpDownCounter:
 *                   <pre>
 *                   meter.upDownCounterBuilder("my-async-counter").buildWithCallback(observableMeasurement -> observableMeasurement.record(..))
 *                   </pre>
 *             </ul>
 *       </ul>
 * </ul>
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/supplementary-guidelines.md#instrument-selection">Instrument
 *     Selection Guidelines</a>
 * @since 1.10.0
 */
@ThreadSafe
public interface Meter {
  /**
   * Constructs a Counter instrument.
   *
   * <p>This is used to build both synchronous instruments and asynchronous instruments (i.e.
   * callbacks).
   *
   * @param name the name of the Counter. Instrument names must consist of 63 or fewer characters
   *     including alphanumeric, _, ., -, and start with a letter.
   * @return a builder for configuring a Counter instrument. Defaults to recording long values, but
   *     may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  LongCounterBuilder counterBuilder(String name);

  /**
   * Constructs an UpDownCounter instrument.
   *
   * <p>This is used to build both synchronous instruments and asynchronous instruments (i.e.
   * callbacks).
   *
   * @param name the name of the UpDownCounter. Instrument names must consist of 63 or fewer
   *     characters including alphanumeric, _, ., -, and start with a letter.
   * @return a builder for configuring an UpDownCounter instrument. Defaults to recording long
   *     values, but may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  LongUpDownCounterBuilder upDownCounterBuilder(String name);

  /**
   * Constructs a Histogram instrument.
   *
   * @param name the name of the Histogram. Instrument names must consist of 63 or fewer characters
   *     including alphanumeric, _, ., -, and start with a letter.
   * @return a builder for configuring a Histogram synchronous instrument. Defaults to recording
   *     double values, but may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  DoubleHistogramBuilder histogramBuilder(String name);

  /**
   * Constructs an Asynchronous Gauge instrument.
   *
   * @param name the name of the Gauge. Instrument names must consist of 63 or fewer characters
   *     including alphanumeric, _, ., -, and start with a letter.
   * @return a builder used for configuring a Gauge instrument. Defaults to recording double values,
   *     but may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  DoubleGaugeBuilder gaugeBuilder(String name);

  /**
   * Constructs a batch callback.
   *
   * <p>Batch callbacks allow a single callback to observe measurements for multiple asynchronous
   * instruments.
   *
   * <p>The callback will be called when the instruments are being observed.
   *
   * <p>Callbacks are expected to abide by the following restrictions:
   *
   * <ul>
   *   <li>Run in a finite amount of time.
   *   <li>Safe to call repeatedly, across multiple threads.
   *   <li>Only observe values to registered instruments (i.e. {@code observableMeasurement} and
   *       {@code additionalMeasurements}
   * </ul>
   *
   * @param callback a callback used to observe values on-demand.
   * @param observableMeasurement Instruments for which the callback may observe values.
   * @param additionalMeasurements Instruments for which the callback may observe values.
   * @since 1.15.0
   */
  default BatchCallback batchCallback(
      Runnable callback,
      ObservableMeasurement observableMeasurement,
      ObservableMeasurement... additionalMeasurements) {
    return DefaultMeter.getInstance()
        .batchCallback(callback, observableMeasurement, additionalMeasurements);
  }
}
