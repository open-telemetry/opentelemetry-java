/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ExplicitBucketHistogramUtilsTest {

  @Test
  void defaultBucketBoundaries() {
    assertThat(ExplicitBucketHistogramUtils.getDefaultBucketBoundaries())
        .isEqualTo(Arrays.asList(0d, 5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 1_000d));
  }
}
