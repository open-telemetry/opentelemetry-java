/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal.asExemplarFilterInternal;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class InstrumentBuilderTest {

  public static final MeterProviderSharedState PROVIDER_SHARED_STATE =
      MeterProviderSharedState.create(
          TestClock.create(),
          Resource.getDefault(),
          asExemplarFilterInternal(ExemplarFilter.alwaysOff()),
          0);
  static final InstrumentationScopeInfo SCOPE = InstrumentationScopeInfo.create("scope-name");
  public static final SdkMeter SDK_METER =
      new SdkMeter(
          PROVIDER_SHARED_STATE, SCOPE, Collections.emptyList(), MeterConfig.defaultConfig());

  @Test
  void stringRepresentation() {
    InstrumentBuilder builder =
        new InstrumentBuilder(
                "instrument-name", InstrumentType.COUNTER, InstrumentValueType.LONG, SDK_METER)
            .setDescription("instrument-description")
            .setUnit("instrument-unit")
            .setAdviceBuilder(Advice.builder());
    assertThat(builder.toString())
        .isEqualTo(
            "InstrumentBuilder{"
                + "descriptor="
                + "InstrumentDescriptor{"
                + "name=instrument-name, "
                + "description=instrument-description, "
                + "unit=instrument-unit, "
                + "type=COUNTER, "
                + "valueType=LONG, "
                + "advice=Advice{explicitBucketBoundaries=null, attributes=null}"
                + "}}");
  }

  @Test
  void toStringHelper() {
    InstrumentBuilder builder =
        new InstrumentBuilder(
                "instrument-name", InstrumentType.HISTOGRAM, InstrumentValueType.DOUBLE, SDK_METER)
            .setDescription("instrument-description")
            .setUnit("instrument-unit")
            .setAdviceBuilder(Advice.builder());

    assertThat(builder.toStringHelper("FooBuilder"))
        .isEqualTo(
            "FooBuilder{"
                + "descriptor="
                + "InstrumentDescriptor{"
                + "name=instrument-name, "
                + "description=instrument-description, "
                + "unit=instrument-unit, "
                + "type=HISTOGRAM, "
                + "valueType=DOUBLE, "
                + "advice=Advice{explicitBucketBoundaries=null, attributes=null}"
                + "}}");
  }
}
