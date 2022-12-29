/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class AbstractInstrumentBuilderTest {

  @Test
  void stringRepresentation() {
    InstrumentationScopeInfo scope = InstrumentationScopeInfo.create("scope-name");
    TestInstrumentBuilder builder =
        new TestInstrumentBuilder(
            MeterProviderSharedState.create(
                TestClock.create(), Resource.getDefault(), ExemplarFilter.alwaysOff(), 0),
            MeterSharedState.create(scope, Collections.emptyList()),
            InstrumentType.COUNTER,
            InstrumentValueType.LONG,
            "instrument-name",
            "instrument-description",
            "instrument-unit");
    assertThat(builder.toString())
        .isEqualTo(
            "TestInstrumentBuilder{"
                + "descriptor="
                + "InstrumentDescriptor{"
                + "name=instrument-name, "
                + "description=instrument-description, "
                + "unit=instrument-unit, "
                + "type=COUNTER, "
                + "valueType=LONG"
                + "}}");
  }

  private static class TestInstrumentBuilder
      extends AbstractInstrumentBuilder<TestInstrumentBuilder> {

    TestInstrumentBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        InstrumentType type,
        InstrumentValueType valueType,
        String name,
        String description,
        String unit) {
      super(meterProviderSharedState, meterSharedState, type, valueType, name, description, unit);
    }

    @Override
    protected TestInstrumentBuilder getThis() {
      return this;
    }
  }
}
