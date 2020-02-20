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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AbstractInstrument.Builder}. */
@RunWith(JUnit4.class)
public class AbstractInstrumentBuilderTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<String> LABEL_KEY = Collections.singletonList("key");
  private static final Map<String, String> CONSTANT_LABELS =
      Collections.singletonMap("key_2", "value_2");
  private static final MeterProviderSharedState METER_SHARED_STATE =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.getEmpty();

  @Test
  public void preventNull_Name() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    new TestInstrumentBuilder(null, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO).build();
  }

  @Test
  public void preventEmpty_Name() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    new TestInstrumentBuilder("", METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
  }

  @Test
  public void checkCorrect_Name() {
    new TestInstrumentBuilder("a", METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
    new TestInstrumentBuilder("METRIC_name", METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
    new TestInstrumentBuilder("metric.name_01", METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
    new TestInstrumentBuilder("metric_name.01", METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    new TestInstrumentBuilder(
        "01.metric_name_01", METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
  }

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    new TestInstrumentBuilder("\2", METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO).build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[AbstractInstrument.Builder.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(AbstractInstrument.Builder.ERROR_MESSAGE_INVALID_NAME);
    new TestInstrumentBuilder(longName, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
        .setDescription(null)
        .build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
        .setUnit(null)
        .build();
  }

  @Test
  public void preventNull_LabelKeys() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys");
    new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
        .setLabelKeys(null)
        .build();
  }

  @Test
  public void preventNull_LabelKey() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKey");
    new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
        .setLabelKeys(Collections.<String>singletonList(null))
        .build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
        .setConstantLabels(null)
        .build();
  }

  @Test
  public void defaultValue() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
    TestInstrument testInstrument = testInstrumentBuilder.build();
    assertThat(testInstrument).isInstanceOf(TestInstrument.class);
    assertThat(testInstrument.getDescriptor().getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescriptor().getDescription()).isEmpty();
    assertThat(testInstrument.getDescriptor().getUnit()).isEqualTo("1");
    assertThat(testInstrument.getDescriptor().getLabelKeys()).isEmpty();
    assertThat(testInstrument.getDescriptor().getConstantLabels()).isEmpty();
  }

  @Test
  public void setAndGetValues() {
    TestInstrumentBuilder testInstrumentBuilder =
        new TestInstrumentBuilder(NAME, METER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setLabelKeys(LABEL_KEY)
            .setConstantLabels(CONSTANT_LABELS);
    assertThat(testInstrumentBuilder.getMeterProviderSharedState()).isEqualTo(METER_SHARED_STATE);
    assertThat(testInstrumentBuilder.getInstrumentationLibraryInfo())
        .isEqualTo(INSTRUMENTATION_LIBRARY_INFO);

    TestInstrument testInstrument = testInstrumentBuilder.build();
    assertThat(testInstrument).isInstanceOf(TestInstrument.class);
    assertThat(testInstrument.getDescriptor().getName()).isEqualTo(NAME);
    assertThat(testInstrument.getDescriptor().getDescription()).isEqualTo(DESCRIPTION);
    assertThat(testInstrument.getDescriptor().getUnit()).isEqualTo(UNIT);
    assertThat(testInstrument.getDescriptor().getLabelKeys()).isEqualTo(LABEL_KEY);
    assertThat(testInstrument.getDescriptor().getConstantLabels()).isEqualTo(CONSTANT_LABELS);
  }

  private static final class TestInstrumentBuilder
      extends AbstractInstrument.Builder<TestInstrumentBuilder, TestInstrument> {
    TestInstrumentBuilder(
        String name,
        MeterProviderSharedState sharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo) {
      super(name, sharedState, instrumentationLibraryInfo);
    }

    @Override
    TestInstrumentBuilder getThis() {
      return this;
    }

    @Override
    public TestInstrument build() {
      return new TestInstrument(
          getInstrumentDescriptor(),
          getMeterProviderSharedState(),
          getInstrumentationLibraryInfo());
    }
  }

  private static final class TestInstrument extends AbstractInstrument {
    TestInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo) {
      super(descriptor, meterProviderSharedState, instrumentationLibraryInfo, null);
    }

    @Override
    List<MetricData> collect() {
      return Collections.emptyList();
    }
  }
}
