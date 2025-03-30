/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import org.junit.jupiter.api.Test;

public class SummaryPointDataTest {

  @Test
  void create() {
    Attributes KV_ATTR = Attributes.of(stringKey("k"), "v");
    SummaryPointData summaryPointData =
        ImmutableSummaryPointData.create(
            123, 456, KV_ATTR, 5, 14.2, singletonList(ImmutableValueAtQuantile.create(0.0, 1.1)));
    assertThat(summaryPointData.getStartEpochNanos()).isEqualTo(123);
    assertThat(summaryPointData.getEpochNanos()).isEqualTo(456);
    assertThat(summaryPointData.getAttributes()).isEqualTo(KV_ATTR);
    assertThat(summaryPointData.getCount()).isEqualTo(5);
    assertThat(summaryPointData.getSum()).isEqualTo(14.2);
  }
}
