/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.metrics.AbstractInstrument.Builder.ERROR_MESSAGE_INVALID_NAME;
import static io.opentelemetry.sdk.metrics.AbstractInstrument.Builder.NAME_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.Labels;
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
  private static final Labels CONSTANT_LABELS = Labels.of("key_2", "value_2");
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
    char[] chars = new char[NAME_MAX_LENGTH + 1];
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
  void preventNull_ConstantLabels() {
    assertThrows(
        NullPointerException.class,
        () ->
            new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
                .setConstantLabels(null)
                .build(),
        "constantLabels");
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
    assertThat(testInstrument.getDescriptor().getConstantLabels().isEmpty()).isTrue();
  }

  @Test
  void setAndGetValues() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_PROVIDER_SHARED_STATE, METER_SHARED_STATE)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS);
    assertThat(testInstrumentBuilder.getMeterProviderSharedState())
        .isSameAs(METER_PROVIDER_SHARED_STATE);
    assertThat(testInstrumentBuilder.getMeterSharedState()).isSameAs(METER_SHARED_STATE);

    TestInstrument testInstrument = testInstrumentBuilder.build();
    assertThat(testInstrument).isInstanceOf(TestInstrument.class);
    assertThat(testInstrument.getDescriptor().getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescriptor().getDescription()).isEqualTo(DESCRIPTION);
    assertThat(testInstrument.getDescriptor().getUnit()).isEqualTo(UNIT);
    assertThat(testInstrument.getDescriptor().getConstantLabels()).isEqualTo(CONSTANT_LABELS);
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
          getInstrumentDescriptor(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG),
          getMeterProviderSharedState(),
          getMeterSharedState());
    }
  }

  private static final class TestInstrument extends AbstractInstrument {
    TestInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(descriptor, meterProviderSharedState, meterSharedState, null);
    }

    @Override
    List<MetricData> collectAll() {
      return Collections.emptyList();
    }
  }
}
