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
 *             use a counter:
 *             <pre>meter.counterBuilder("my-counter").build()</pre>
 *         <li>If the value is NOT monotonically increasing (the delta value can be positive,
 *             negative, or zero)) - use an up down counter:
 *             <pre>meter.upDownCounterBuilder("my-up-down-counter").build()</pre>
 *       </ul>
 *   <li>I want to <b>record</b> or <b>time</b> something, and the <b>statistics</b> about this
 *       thing are likely to be meaningful - use a histogram:
 *       <pre>meter.histogramBuilder("my-histogram").build()</pre>
 *   <li>I want to <b>measure</b> something (by reporting an absolute value):
 *       <ul>
 *         <li>If it makes NO sense to add up the values across different sets of attributes, use an
 *             asynchronous gauge:
 *             <pre>
 *             meter.gaugeBuilder("my-gauge").buildWithCallback(observableMeasurement -> observableMeasurement.record(..))
 *             </pre>
 *         <li>If it makes sense to add up the values across different sets of attributes:
 *             <ul>
 *               <li>If the value is monotonically increasing - use an asynchronous counter:
 *                   <pre>
 *                   meter.counterBuilder("my-async-counter").buildWithCallback(observableMeasurement -> observableMeasurement.record(..))
 *                   </pre>
 *               <li>If the value is NOT monotonically increasing - use an asynchronous up down
 *                   counter:
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
 */
@ThreadSafe
public interface Meter {
  /**
   * Constructs a counter instrument.
   *
   * <p>This is used to build both synchronous instruments and asynchronous instruments (i.e.
   * callbacks).
   *
   * @param name the name of the counter. Instrument names must consist of 63 or fewer characters
   *     including alphanumeric, _, ., -, and start with a letter.
   * @return a builder for configuring a counter instrument. Defaults to recording long values, but
   *     may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  LongCounterBuilder counterBuilder(String name);

  /**
   * Constructs an up down counter instrument.
   *
   * <p>This is used to build both synchronous instruments and asynchronous instruments (i.e.
   * callbacks).
   *
   * @param name the name of the up down counter. Instrument names must consist of 63 or fewer
   *     characters including alphanumeric, _, ., -, and start with a letter.
   * @return a builder for configuring an up down counter instrument. Defaults to recording long
   *     values, but may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  LongUpDownCounterBuilder upDownCounterBuilder(String name);

  /**
   * Constructs a histogram instrument.
   *
   * @param name the name of the histogram. Instrument names must consist of 63 or fewer characters
   *     including alphanumeric, _, ., -, and start with a letter.
   * @return a builder for configuring a histogram synchronous instrument. Defaults to recording
   *     double values, but may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  DoubleHistogramBuilder histogramBuilder(String name);

  /**
   * Constructs an asynchronous gauge instrument.
   *
   * @param name the name of the gauge. Instrument names must consist of 63 or fewer characters
   *     including alphanumeric, _, ., -, and start with a letter.
   * @return a builder used for configuring a gauge instrument. Defaults to recording double values,
   *     but may be changed.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-naming-rule">Instrument
   *     Naming Rule</a>
   */
  DoubleGaugeBuilder gaugeBuilder(String name);
}
