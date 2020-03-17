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

package io.opentelemetry.metrics;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultMeter}. */
@RunWith(JUnit4.class)
public final class DefaultMeterTest {
  private static final Meter defaultMeter = DefaultMeter.getInstance();
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopAddDoubleCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.doubleCounterBuilder(null);
  }

  @Test
  public void noopAddLongCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.longCounterBuilder(null);
  }

  @Test
  public void noopAddMeasureDouble_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.doubleMeasureBuilder(null);
  }

  @Test
  public void noopAddMeasureLong_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.longMeasureBuilder(null);
  }

  @Test
  public void noopAddDoubleCumulative_EmptyName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    defaultMeter.doubleCounterBuilder("");
  }

  @Test
  public void noopAddLongCumulative_EmptyName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    defaultMeter.longCounterBuilder("");
  }

  @Test
  public void noopAddMeasureDouble_EmptyName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    defaultMeter.doubleMeasureBuilder("");
  }

  @Test
  public void noopAddMeasureLong_EmptyName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    defaultMeter.longMeasureBuilder("");
  }

  @Test
  public void noopAddObserverDouble_EmptyName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    defaultMeter.doubleObserverBuilder("");
  }

  @Test
  public void noopAddObserverLong_EmptyName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name");
    defaultMeter.longObserverBuilder("");
  }

  @Test
  public void testNewBatchRecorder_badLabelSet() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("key/value");
    defaultMeter.newBatchRecorder("key");
  }
}
