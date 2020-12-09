/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.metrics.AbstractInstrument.Builder.ERROR_MESSAGE_INVALID_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AbstractInstrument.Builder}. */
class AbstractInstrumentBuilderTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final MeterSharedState METER_SHARED_STATE =
      MeterSharedState.create(InstrumentationLibraryInfo.getEmpty());

  @Test
  void preventNull_Name() {
    assertThrows(
        NullPointerException.class,
        () ->
            new TestInstrumentBuilder(null, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
                .build(),
        "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new TestInstrumentBuilder("", METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE),
        "Name");
  }

  @Test
  void checkCorrect_Name() {
    new TestInstrumentBuilder("a", METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE);
    new TestInstrumentBuilder("METRIC_name", METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE);
    new TestInstrumentBuilder("metric.name_01", METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE);
    new TestInstrumentBuilder("metric_name.01", METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new TestInstrumentBuilder(
                "01.metric_name_01", METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE),
        "Name");
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new TestInstrumentBuilder("\2", METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
                .build());
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new TestInstrumentBuilder(longName, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
                .build(),
        ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () ->
            new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
                .setDescription(null)
                .build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () ->
            new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
                .setUnit(null)
                .build(),
        "unit");
  }

  @Test
  void defaultValue() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE);
    TestInstrument testInstrument = testInstrumentBuilder.build();
    assertThat(testInstrument).isInstanceOf(TestInstrument.class);
    assertThat(testInstrument.getDescriptor().getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescriptor().getDescription()).isEmpty();
    assertThat(testInstrument.getDescriptor().getUnit()).isEqualTo("1");
  }

  @Test
  void setAndGetValues() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT);

    TestInstrument testInstrument = testInstrumentBuilder.build();
    assertThat(testInstrument).isInstanceOf(TestInstrument.class);
    assertThat(testInstrument.getDescriptor().getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescriptor().getDescription()).isEqualTo(DESCRIPTION);
    assertThat(testInstrument.getDescriptor().getUnit()).isEqualTo(UNIT);
    assertThat(testInstrument.getDescriptor().getType()).isEqualTo(InstrumentType.UP_DOWN_COUNTER);
    assertThat(testInstrument.getDescriptor().getValueType()).isEqualTo(InstrumentValueType.LONG);
  }

  private static final class TestInstrumentBuilder
      extends AbstractInstrument.Builder<TestInstrumentBuilder> {
    TestInstrumentBuilder(
        String name, MeterProviderSharedState sharedState, MeterSharedState meterSharedState) {
      super(name, sharedState, meterSharedState, null);
    }

    @Override
    TestInstrumentBuilder getThis() {
      return this;
    }

    @Override
    public TestInstrument build() {
      return new TestInstrument(
          getInstrumentDescriptor(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG));
    }
  }

  private static final class TestInstrument extends AbstractInstrument {
    TestInstrument(InstrumentDescriptor descriptor) {
      super(descriptor);
    }

    @Override
    List<MetricData> collectAll() {
      return Collections.emptyList();
    }
  }
}
