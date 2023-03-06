/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class AbstractInstrumentBuilderTest {

  @RegisterExtension
  LogCapturer apiUsageLogs =
      LogCapturer.create().captureForLogger(AbstractInstrumentBuilder.LOGGER_NAME);

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

  @Test
  void checkValidInstrumentUnit_InvalidUnitLogs() {
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit("日", " suffix")).isFalse();
    apiUsageLogs.assertContains(
        "Unit \"日\" is invalid. Instrument unit must be 63 or fewer ASCII characters." + " suffix");
  }

  @Test
  void checkValidInstrumentUnit() {
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit("a")).isTrue();
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit("A")).isTrue();
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit("foo129")).isTrue();
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit("!@#$%^&*()")).isTrue();
    assertThat(
            AbstractInstrumentBuilder.checkValidInstrumentUnit(
                new String(new char[63]).replace('\0', 'a')))
        .isTrue();

    // Empty and null not allowed
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit(null)).isFalse();
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit("")).isFalse();
    // Non-ascii characters
    assertThat(AbstractInstrumentBuilder.checkValidInstrumentUnit("日")).isFalse();
    // Must be 63 characters or fewer
    assertThat(
            AbstractInstrumentBuilder.checkValidInstrumentUnit(
                new String(new char[64]).replace('\0', 'a')))
        .isFalse();
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
