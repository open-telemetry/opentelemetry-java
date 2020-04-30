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

package io.opentelemetry.context;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link GlobalContextPropagators}. */
@RunWith(JUnit4.class)
public final class GlobalPropagatorsTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @After
  public void tearDown() {
    GlobalPropagators.reset();
  }

  @Test
  public void testDefault() {
    assertThat(GlobalPropagators.get()).isInstanceOf(DefaultContextPropagators.class);
    assertThat(GlobalPropagators.get()).isSameInstanceAs(GlobalPropagators.get());
  }

  @Test
  public void testSet() {
    ContextPropagators props = DefaultContextPropagators.builder().build();
    GlobalPropagators.set(props);
    assertThat(GlobalPropagators.get()).isSameInstanceAs(props);
  }

  @Test
  public void testSetMultiple() {
    ContextPropagators props = DefaultContextPropagators.builder().build();
    GlobalPropagators.set(props);
    assertThat(GlobalPropagators.get()).isSameInstanceAs(props);

    ContextPropagators props2 = DefaultContextPropagators.builder().build();
    GlobalPropagators.set(props2);
    assertThat(GlobalPropagators.get()).isSameInstanceAs(props2);
  }

  @Test
  public void testSetNull() {
    thrown.expect(NullPointerException.class);
    GlobalPropagators.set(null);
  }
}
