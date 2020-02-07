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
import io.opentelemetry.trace.TraceState.Entry;
import java.util.Arrays;
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
  private final TraceState firstTraceState = EMPTY.toBuilder().set(FIRST_KEY, FIRST_VALUE).build();
  private final TraceState secondTraceState =
      EMPTY.toBuilder().set(SECOND_KEY, SECOND_VALUE).build();
  private final TraceState multiValueTraceState =
      EMPTY.toBuilder().set(FIRST_KEY, FIRST_VALUE).set(SECOND_KEY, SECOND_VALUE).build();

  @Test
  public void get() {
    assertThat(firstTraceState.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(secondTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(multiValueTraceState.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(multiValueTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
  }

  @Test
  public void getEntries() {
    assertThat(firstTraceState.getEntries()).containsExactly(Entry.create(FIRST_KEY, FIRST_VALUE));
    assertThat(secondTraceState.getEntries())
        .containsExactly(Entry.create(SECOND_KEY, SECOND_VALUE));
    assertThat(multiValueTraceState.getEntries())
        .containsExactly(
            Entry.create(FIRST_KEY, FIRST_VALUE), Entry.create(SECOND_KEY, SECOND_VALUE));
  }

  @Test
  public void disallowsNullKey() {
    thrown.expect(NullPointerException.class);
    EMPTY.toBuilder().set(null, FIRST_VALUE).build();
  }

  @Test
  public void invalidFirstKeyCharacter() {
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set("$_key", FIRST_VALUE).build();
  }

  @Test
  public void firstKeyCharacterDigitIsAllowed() {
    TraceState result = EMPTY.toBuilder().set("1_key", FIRST_VALUE).build();
    assertThat(result.get("1_key")).isEqualTo(FIRST_VALUE);
  }

  @Test
  public void invalidKeyCharacters() {
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set("kEy_1", FIRST_VALUE).build();
  }

  @Test
  public void testValidAtSignVendorNamePrefix() {
    TraceState result = EMPTY.toBuilder().set("1@nr", FIRST_VALUE).build();
    assertThat(result.get("1@nr")).isEqualTo(FIRST_VALUE);
  }

  @Test
  public void testMultipleAtSignNotAllowed() {
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set("1@n@r@", FIRST_VALUE).build();
  }

  @Test
  public void invalidKeySize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longKey = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set(longKey, FIRST_VALUE).build();
  }

  @Test
  public void allAllowedKeyCharacters() {
    StringBuilder stringBuilder = new StringBuilder();
    for (char c = 'a'; c <= 'z'; c++) {
      stringBuilder.append(c);
    }
    for (char c = '0'; c <= '9'; c++) {
      stringBuilder.append(c);
    }
    stringBuilder.append('_');
    stringBuilder.append('-');
    stringBuilder.append('*');
    stringBuilder.append('/');
    String allowedKey = stringBuilder.toString();
    assertThat(EMPTY.toBuilder().set(allowedKey, FIRST_VALUE).build().get(allowedKey))
        .isEqualTo(FIRST_VALUE);
  }

  @Test
  public void disallowsNullValue() {
    thrown.expect(NullPointerException.class);
    EMPTY.toBuilder().set(FIRST_KEY, null).build();
  }

  @Test
  public void valueCannotContainEqual() {
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set(FIRST_KEY, "my_vakue=5").build();
  }

  @Test
  public void valueCannotContainComma() {
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set(FIRST_KEY, "first,second").build();
  }

  @Test
  public void valueCannotContainTrailingSpaces() {
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set(FIRST_KEY, "first ").build();
  }

  @Test
  public void invalidValueSize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longValue = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    EMPTY.toBuilder().set(FIRST_KEY, longValue).build();
  }

  @Test
  public void allAllowedValueCharacters() {
    StringBuilder stringBuilder = new StringBuilder();
    for (char c = ' ' /* '\u0020' */; c <= '~' /* '\u007E' */; c++) {
      if (c == ',' || c == '=') {
        continue;
      }
      stringBuilder.append(c);
    }
    String allowedValue = stringBuilder.toString();
    assertThat(EMPTY.toBuilder().set(FIRST_KEY, allowedValue).build().get(FIRST_KEY))
        .isEqualTo(allowedValue);
  }

  @Test
  public void addEntry() {
    assertThat(firstTraceState.toBuilder().set(SECOND_KEY, SECOND_VALUE).build())
        .isEqualTo(multiValueTraceState);
  }

  @Test
  public void updateEntry() {
    assertThat(firstTraceState.toBuilder().set(FIRST_KEY, SECOND_VALUE).build().get(FIRST_KEY))
        .isEqualTo(SECOND_VALUE);
    TraceState updatedMultiValueTraceState =
        multiValueTraceState.toBuilder().set(FIRST_KEY, SECOND_VALUE).build();
    assertThat(updatedMultiValueTraceState.get(FIRST_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(updatedMultiValueTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
  }

  @Test
  public void addAndUpdateEntry() {
    assertThat(
            firstTraceState
                .toBuilder()
                .set(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .set(SECOND_KEY, FIRST_VALUE) // add a new entry
                .build()
                .getEntries())
        .containsExactly(
            Entry.create(FIRST_KEY, SECOND_VALUE), Entry.create(SECOND_KEY, FIRST_VALUE));
  }

  @Test
  public void addSameKey() {
    assertThat(
            EMPTY
                .toBuilder()
                .set(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .set(FIRST_KEY, FIRST_VALUE) // add a new entry
                .build()
                .getEntries())
        .containsExactly(Entry.create(FIRST_KEY, FIRST_VALUE));
  }

  @Test
  public void remove() {
    assertThat(multiValueTraceState.toBuilder().remove(SECOND_KEY).build())
        .isEqualTo(firstTraceState);
  }

  @Test
  public void addAndRemoveEntry() {
    assertThat(
            EMPTY
                .toBuilder()
                .set(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .remove(FIRST_KEY) // add a new entry
                .build())
        .isEqualTo(EMPTY);
  }

  @Test
  public void remove_NullNotAllowed() {
    thrown.expect(NullPointerException.class);
    multiValueTraceState.toBuilder().remove(null).build();
  }

  @Test
  public void traceState_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(EMPTY, EMPTY);
    tester.addEqualityGroup(firstTraceState, EMPTY.toBuilder().set(FIRST_KEY, FIRST_VALUE).build());
    tester.addEqualityGroup(
        secondTraceState, EMPTY.toBuilder().set(SECOND_KEY, SECOND_VALUE).build());
    tester.testEquals();
  }

  @Test
  public void traceState_ToString() {
    assertThat(EMPTY.toString()).isEqualTo("TraceState{entries=[]}");
  }
}
