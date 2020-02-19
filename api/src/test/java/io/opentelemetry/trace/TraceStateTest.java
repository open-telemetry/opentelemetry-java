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

package io.opentelemetry.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceState}. */
@RunWith(JUnit4.class)
public class TraceStateTest {

  private static final String FIRST_KEY = "key_1";
  private static final String SECOND_KEY = "key_2";
  private static final String FIRST_VALUE = "value.1";
  private static final String SECOND_VALUE = "value.2";

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final TraceState EMPTY = TraceState.builder().build();
  private final TraceState firstTraceState = EMPTY.toBuilder().add(FIRST_KEY + FIRST_VALUE).build();
  private final TraceState secondTraceState =
      EMPTY.toBuilder().add(SECOND_KEY + SECOND_VALUE).build();
  private final TraceState multiValueTraceState =
      EMPTY.toBuilder().add(FIRST_KEY + FIRST_VALUE).add(SECOND_KEY + SECOND_VALUE).build();

  @Test
  public void getEntries() {
    assertThat(firstTraceState.getEntries()).containsExactly(FIRST_KEY + FIRST_VALUE);
    assertThat(secondTraceState.getEntries()).containsExactly(SECOND_KEY + SECOND_VALUE);
    assertThat(multiValueTraceState.getEntries())
        .containsExactly(FIRST_KEY + FIRST_VALUE, SECOND_KEY + SECOND_VALUE);
  }

  @Test
  public void disallowsNullValue() {
    thrown.expect(NullPointerException.class);
    EMPTY.toBuilder().add(null).build();
  }

  @Test
  public void addEntry() {
    assertThat(firstTraceState.toBuilder().add(SECOND_KEY + SECOND_VALUE).build())
        .isEqualTo(multiValueTraceState);
  }

  @Test
  public void addAndUpdateEntry() {
    assertThat(
            firstTraceState
                .toBuilder()
                .add(FIRST_KEY + SECOND_VALUE) // update the existing entry
                .add(SECOND_KEY + FIRST_VALUE) // add a new entry
                .build()
                .getEntries())
        .containsExactly(
            FIRST_KEY + SECOND_VALUE, FIRST_KEY + FIRST_VALUE, SECOND_KEY + FIRST_VALUE);
  }

  @Test
  public void addSameKey() {
    assertThat(
            EMPTY
                .toBuilder()
                .add(FIRST_KEY + SECOND_VALUE) // update the existing entry
                .add(FIRST_KEY + FIRST_VALUE) // add a new entry
                .build()
                .getEntries())
        .containsExactly(FIRST_KEY + SECOND_VALUE, FIRST_KEY + FIRST_VALUE);
  }

  @Test
  public void traceState_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(EMPTY, EMPTY);
    tester.addEqualityGroup(
        firstTraceState, EMPTY.toBuilder().add(FIRST_KEY + FIRST_VALUE).build());
    tester.addEqualityGroup(
        secondTraceState, EMPTY.toBuilder().add(SECOND_KEY + SECOND_VALUE).build());
    tester.testEquals();
  }

  @Test
  public void traceState_ToString() {
    assertThat(EMPTY.toString()).isEqualTo("TraceState{entries=[]}");
  }
}
