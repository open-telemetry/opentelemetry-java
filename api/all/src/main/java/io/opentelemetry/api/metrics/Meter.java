/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Provides instruments used to produce metrics.
 *
 * <p>Instruments are obtained through builders provided by this interface. Each builder has a
 * default "type" associated with recordings that may be changed.
 *
 * <p>A Meter is generally associated with an instrumentation scope, e.g. "I monitor apache
 * httpclient".
 *
 * <p>Choosing an instrument can be hard, but here's a rule of thumb for selecting the right
 * instrument:
 *
 * <ul>
 *   <li>I want to <b>count</b> something.
 *       <ul>
 *         <li>The value is always increasing / I want to track its <b>rate</b>.<br>
 *             Use {@link #counterBuilder(String)}
 *         <li>The value is not always increasing.<br>
 *             Use {@link #upDownCounterBuilder(String)}
 *       </ul>
 *   <li>I want to <b>time</b> something, or record measurements where the statistics are important
 *       (e.g. latency).<br>
 *       <b>Use {@link #histogramBuilder(String)}</b>
 *   <li>I want to measure something by sampling a value stored elsewhere. <br>
 *       Use {@link #gaugeBuilder(String)}
 * </ul>
 */
@ThreadSafe
public interface Meter {
  /**
   * Constructs a counter instrument.
   *
   * <p>This is used to build both synchronous (in-context) instruments and asynchronous (callback)
   * instruments.
   *
   * @param name the name used for the counter.
   * @return a builder for configuring a new Counter instrument. Defaults to recording long values,
   *     but may be changed.
   */
  LongCounterBuilder counterBuilder(String name);

  /**
   * Constructs an up-down-counter instrument.
   *
   * <p>This is used to build both synchronous (in-context) instruments and asynchronous (callback)
   * instruments.
   *
   * @param name the name used for the counter.
   * @return a builder for configuring a new Counter synchronous instrument. Defaults to recording
   *     long values, but may be changed.
   */
  LongUpDownCounterBuilder upDownCounterBuilder(String name);

  /**
   * Constructs a Histogram instrument.
   *
   * @param name the name used for the counter.
   * @return a builder for configuring a new Histogram synchronous instrument. Defaults to recording
   *     double values, but may be changed.
   */
  DoubleHistogramBuilder histogramBuilder(String name);

  /**
   * Constructs an asynchronous gauge.
   *
   * @return a builder used for configuring how to report gauge measurements on demand.
   */
  DoubleGaugeBuilder gaugeBuilder(String name);

  /**
   * Constructs a batch callback.
   *
   * <p>Batch callbacks allow a single callback to observe measurements for multiple asynchronous
   * instruments.
   *
   * <p>The callback will be called when the {@link Meter} is being observed.
   *
   * <p>Callbacks are expected to abide by the following restrictions:
   *
   * <ul>
   *   <li>Run in a finite amount of time.
   *   <li>Safe to call repeatedly, across multiple threads.
   *   <li>Only observe values to registered instruments (i.e. {@code observableMeasurement} and
   *       {@code observableMeasurements}
   * </ul>
   *
   * @param callback a callback used to observe values on-demand.
   * @param observableMeasurement Instruments for which the callback may observe values.
   * @param observableMeasurements Instruments for which the callback may observe values.
   */
  default BatchCallback batchCallback(
      Runnable callback,
      ObservableMeasurement observableMeasurement,
      ObservableMeasurement... observableMeasurements) {
    return DefaultMeter.getInstance()
        .batchCallback(callback, observableMeasurement, observableMeasurements);
  }
}
