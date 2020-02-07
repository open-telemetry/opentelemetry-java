/*
 * Copyright 2020, OpenTelemetry Authors
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

import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.Measure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AbstractMeasureBuilder}. */
@RunWith(JUnit4.class)
public class AbstractMeasureBuilderTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";

  @Test
  public void defaultValue() {
    TestInstrumentBuilder testMetricBuilder = TestInstrumentBuilder.newBuilder(NAME);
    assertThat(testMetricBuilder.getName()).isEqualTo(NAME);
    assertThat(testMetricBuilder.getDescription()).isEmpty();
    assertThat(testMetricBuilder.getUnit()).isEqualTo("1");
    assertThat(testMetricBuilder.getLabelKeys()).isEmpty();
    assertThat(testMetricBuilder.getConstantLabels()).isEmpty();
    assertThat(testMetricBuilder.isAbsolute()).isTrue();
    assertThat(testMetricBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  @Test
  public void setAndGetValues() {
    TestInstrumentBuilder testMetricBuilder =
        TestInstrumentBuilder.newBuilder(NAME).setAbsolute(false);
    assertThat(testMetricBuilder.getName()).isEqualTo(NAME);
    assertThat(testMetricBuilder.isAbsolute()).isFalse();
    assertThat(testMetricBuilder.build()).isInstanceOf(TestInstrument.class);
  }

  private static final class TestInstrumentBuilder
      extends AbstractMeasureBuilder<TestInstrumentBuilder, TestInstrument> {
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

  private static final class TestInstrument implements Measure<TestBoundMeasure> {
    private static final TestBoundMeasure HANDLE = new TestBoundMeasure();

    @Override
    public TestBoundMeasure bind(LabelSet labelSet) {
      return HANDLE;
    }
  }

  private static final class TestBoundMeasure implements Measure.Bound {
    @Override
    public void unbind() {}
  }
}
