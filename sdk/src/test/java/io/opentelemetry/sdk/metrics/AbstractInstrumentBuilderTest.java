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

import io.opentelemetry.metrics.Instrument;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AbstractInstrumentBuilder}. */
@RunWith(JUnit4.class)
public class AbstractInstrumentBuilderTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<String> LABEL_KEY = Collections.singletonList("key");
  private static final Map<String, String> CONSTANT_LABELS =
      Collections.singletonMap("key_2", "value_2");

  @Test
  public void preventNull_Name() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    TestInstrumentBuilder.newBuilder(null);
  }

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    TestInstrumentBuilder.newBuilder("\2");
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[AbstractInstrumentBuilder.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(AbstractInstrumentBuilder.ERROR_MESSAGE_INVALID_NAME);
    TestInstrumentBuilder.newBuilder(longName);
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    TestInstrumentBuilder.newBuilder("metric").setDescription(null);
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    TestInstrumentBuilder.newBuilder("metric").setUnit(null);
  }

  @Test
  public void preventNull_LabelKeys() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys");
    TestInstrumentBuilder.newBuilder("metric").setLabelKeys(null);
  }

  @Test
  public void preventNull_LabelKey() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKey");
    TestInstrumentBuilder.newBuilder("metric")
        .setLabelKeys(Collections.<String>singletonList(null));
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    TestInstrumentBuilder.newBuilder("metric").setConstantLabels(null);
  }

  @Test
  public void defaultValue() {
    TestInstrumentBuilder testMetricBuilder = TestInstrumentBuilder.newBuilder(NAME);
    assertThat(testMetricBuilder.getName()).isEqualTo(NAME);
    assertThat(testMetricBuilder.getDescription()).isEmpty();
    assertThat(testMetricBuilder.getUnit()).isEqualTo("1");
    assertThat(testMetricBuilder.getLabelKeys()).isEmpty();
    assertThat(testMetricBuilder.getConstantLabels()).isEmpty();
    assertThat(testMetricBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  @Test
  public void setAndGetValues() {
    TestInstrumentBuilder testMetricBuilder =
        TestInstrumentBuilder.newBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setLabelKeys(LABEL_KEY)
            .setConstantLabels(CONSTANT_LABELS);
    assertThat(testMetricBuilder.getName()).isEqualTo(NAME);
    assertThat(testMetricBuilder.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(testMetricBuilder.getUnit()).isEqualTo(UNIT);
    assertThat(testMetricBuilder.getLabelKeys()).isEqualTo(LABEL_KEY);
    assertThat(testMetricBuilder.getConstantLabels()).isEqualTo(CONSTANT_LABELS);
    assertThat(testMetricBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  private static final class TestInstrumentBuilder
      extends AbstractInstrumentBuilder<TestInstrumentBuilder, TestInstrument> {
    static TestInstrumentBuilder newBuilder(String name) {
      return new TestInstrumentBuilder(name);
    }

    TestInstrumentBuilder(String name) {
      super(name);
    }

    @Override
    TestInstrumentBuilder getThis() {
      return this;
    }

    @Override
    public TestInstrument build() {
      return new TestInstrument();
    }
  }

  private static final class TestInstrument implements Instrument {}
}
