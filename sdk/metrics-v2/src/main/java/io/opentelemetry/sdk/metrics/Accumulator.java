/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

class Accumulator {
  void recordLongAdd(
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      long increment,
      Labels labels) {
    // find/create the appropriate aggregator "record" based on iLI, ID and Labels
    // todo: implement me;
  }

  // called by the Controller, either on an interval, or when a pull-based exporter needs it.
  void collect() {
    // this method will take all the accumulations and pass them on to the Processor
    // it will do this for each of the Aggregators that have been created this collection cycle,
    // doing a snapshot ("synchronized move") on each of them to get the appropriate Accumulation
    // data.
    // todo: implement me
  }
}
