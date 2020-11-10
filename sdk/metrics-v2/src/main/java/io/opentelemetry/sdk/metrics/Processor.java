/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.Collections;

//todo: this probably needs to be an interface eventually, so you can have custom processors, or
//nested processors, or other fancy features.
class Processor {

  void start() {
    // todo: flag that we're within a collection cycle, and should be accumulating values.
  }

  void process(AggregatorKey key, Accumulation accumulation) {
    // todo: implement me!
  }

  Collection<MetricData> finish() {
    // todo: get all the data from the aggregators, transform it into MetricData and return it.
    // Finally mark the collection cycle as complete.
    return Collections.emptyList();
  }
}
