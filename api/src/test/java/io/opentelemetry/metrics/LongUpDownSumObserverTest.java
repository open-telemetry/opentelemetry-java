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

package io.opentelemetry.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.metrics.AsynchronousInstrument.Callback;
import io.opentelemetry.metrics.LongUpDownSumObserver.ResultLongUpDownSumObserver;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongUpDownSumObserver}. */
@RunWith(JUnit4.class)
public class LongUpDownSumObserverTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Map<String, String> CONSTANT_LABELS =
      Collections.singletonMap("key", "value");

  private final Meter meter = OpenTelemetry.getMeter("LongUpDownSumObserverTest");

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    meter.longUpDownSumObserverBuilder("\2").build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[StringUtils.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.longUpDownSumObserverBuilder(longName).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.longUpDownSumObserverBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.longUpDownSumObserverBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.longUpDownSumObserverBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void preventNull_Callback() {
    LongUpDownSumObserver longUpDownSumObserver =
        meter.longUpDownSumObserverBuilder("metric").build();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("callback");
    longUpDownSumObserver.setCallback(null);
  }

  @Test
  public void doesNotThrow() {
    LongUpDownSumObserver longUpDownSumObserver =
        meter
            .longUpDownSumObserverBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    longUpDownSumObserver.setCallback(
        new Callback<ResultLongUpDownSumObserver>() {
          @Override
          public void update(ResultLongUpDownSumObserver result) {}
        });
  }
}
