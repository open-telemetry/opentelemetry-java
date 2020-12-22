/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class TraceStateTest {
  private static final String FIRST_KEY = "key_1";
  private static final String SECOND_KEY = "key_2";
  private static final String FIRST_VALUE = "value.1";
  private static final String SECOND_VALUE = "value.2";

  private static final TraceState EMPTY = TraceState.builder().build();
  private final TraceState firstTraceState = EMPTY.toBuilder().set(FIRST_KEY, FIRST_VALUE).build();
  private final TraceState secondTraceState =
      EMPTY.toBuilder().set(SECOND_KEY, SECOND_VALUE).build();
  private final TraceState multiValueTraceState =
      EMPTY.toBuilder().set(FIRST_KEY, FIRST_VALUE).set(SECOND_KEY, SECOND_VALUE).build();

  @Test
  void get() {
    assertThat(firstTraceState.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(secondTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(multiValueTraceState.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(multiValueTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(firstTraceState.get("dog")).isNull();
  }

  @Test
  void sizeAndEmpty() {
    assertThat(EMPTY.size()).isZero();
    assertThat(EMPTY.isEmpty()).isTrue();

    assertThat(firstTraceState.size()).isOne();
    assertThat(firstTraceState.isEmpty()).isFalse();

    assertThat(multiValueTraceState.size()).isEqualTo(2);
    assertThat(multiValueTraceState.isEmpty()).isFalse();
  }

  @Test
  void forEach() {
    LinkedHashMap<String, String> entries = new LinkedHashMap<>();
    firstTraceState.forEach(entries::put);
    assertThat(entries).containsExactly(entry(FIRST_KEY, FIRST_VALUE));

    entries.clear();
    secondTraceState.forEach(entries::put);
    assertThat(entries).containsExactly(entry(SECOND_KEY, SECOND_VALUE));

    entries.clear();
    multiValueTraceState.forEach(entries::put);
    // Reverse order of input.
    assertThat(entries)
        .containsExactly(entry(SECOND_KEY, SECOND_VALUE), entry(FIRST_KEY, FIRST_VALUE));
  }

  @Test
  void asMap() {
    assertThat(firstTraceState.asMap()).containsExactly(entry(FIRST_KEY, FIRST_VALUE));

    assertThat(secondTraceState.asMap()).containsExactly(entry(SECOND_KEY, SECOND_VALUE));

    // Reverse order of input.
    assertThat(multiValueTraceState.asMap())
        .containsExactly(entry(SECOND_KEY, SECOND_VALUE), entry(FIRST_KEY, FIRST_VALUE));
  }

  @Test
  void disallowsNullKey() {
    assertThat(EMPTY.toBuilder().set(null, FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void disallowsEmptyKey() {
    assertThat(EMPTY.toBuilder().set("", FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void invalidFirstKeyCharacter() {
    assertThat(EMPTY.toBuilder().set("$_key", FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void firstKeyCharacterDigitIsAllowed() {
    // note: a digit is only allowed if the key is in the tenant format (with an '@')
    TraceState result = EMPTY.toBuilder().set("1@tenant", FIRST_VALUE).build();
    assertThat(result.get("1@tenant")).isEqualTo(FIRST_VALUE);
  }

  @Test
  void testValidLongTenantId() {
    TraceState result = EMPTY.toBuilder().set("12345678901234567890@nr", FIRST_VALUE).build();
    assertThat(result.get("12345678901234567890@nr")).isEqualTo(FIRST_VALUE);
  }

  @Test
  void invalidKeyCharacters() {
    assertThat(EMPTY.toBuilder().set("kEy_1", FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void testValidAtSignVendorNamePrefix() {
    TraceState result = EMPTY.toBuilder().set("1@nr", FIRST_VALUE).build();
    assertThat(result.get("1@nr")).isEqualTo(FIRST_VALUE);
  }

  @Test
  void testVendorIdLongerThan13Characters() {
    assertThat(EMPTY.toBuilder().set("1@nrabcdefghijkl", FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void testVendorIdLongerThan13Characters_longTenantId() {
    assertThat(EMPTY.toBuilder().set("12345678901234567890@nrabcdefghijkl", FIRST_VALUE).build())
        .isEqualTo(EMPTY);
  }

  @Test
  void tenantIdLongerThan240Characters() {
    char[] chars = new char[241];
    Arrays.fill(chars, 'a');
    String tenantId = new String(chars);
    assertThat(EMPTY.toBuilder().set(tenantId + "@nr", FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void testNonVendorFormatFirstKeyCharacter() {
    assertThat(EMPTY.toBuilder().set("1acdfrgs", FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void testMultipleAtSignNotAllowed() {
    assertThat(EMPTY.toBuilder().set("1@n@r@", FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void invalidKeySize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longKey = new String(chars);
    assertThat(EMPTY.toBuilder().set(longKey, FIRST_VALUE).build()).isEqualTo(EMPTY);
  }

  @Test
  void allAllowedKeyCharacters() {
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
  void disallowsNullValue() {
    assertThat(EMPTY.toBuilder().set(FIRST_KEY, null).build()).isEqualTo(EMPTY);
  }

  @Test
  void valueCannotContainEqual() {
    assertThat(EMPTY.toBuilder().set(FIRST_KEY, "my_vakue=5").build()).isEqualTo(EMPTY);
  }

  @Test
  void valueCannotContainComma() {
    assertThat(EMPTY.toBuilder().set(FIRST_KEY, "first,second").build()).isEqualTo(EMPTY);
  }

  @Test
  void valueCannotContainTrailingSpaces() {
    assertThat(EMPTY.toBuilder().set(FIRST_KEY, "first ").build()).isEqualTo(EMPTY);
  }

  @Test
  void invalidValueSize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longValue = new String(chars);
    assertThat(EMPTY.toBuilder().set(FIRST_KEY, longValue).build()).isEqualTo(EMPTY);
  }

  @Test
  void allAllowedValueCharacters() {
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
  void notAllowedValueCharacters() {
    assertThat(TraceState.builder().set("foo", "bar,").build()).isEqualTo(EMPTY);
    assertThat(TraceState.builder().set("foo", "bar=").build()).isEqualTo(EMPTY);
    assertThat(TraceState.builder().set("foo", "bar\u0019").build()).isEqualTo(EMPTY);
    assertThat(TraceState.builder().set("foo", "bar\u007F").build()).isEqualTo(EMPTY);
  }

  @Test
  void addEntry() {
    assertThat(firstTraceState.toBuilder().set(SECOND_KEY, SECOND_VALUE).build())
        .isEqualTo(multiValueTraceState);
  }

  @Test
  void updateEntry() {
    assertThat(firstTraceState.toBuilder().set(FIRST_KEY, SECOND_VALUE).build().get(FIRST_KEY))
        .isEqualTo(SECOND_VALUE);
    TraceState updatedMultiValueTraceState =
        multiValueTraceState.toBuilder().set(FIRST_KEY, SECOND_VALUE).build();
    assertThat(updatedMultiValueTraceState.get(FIRST_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(updatedMultiValueTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
  }

  @Test
  void addAndUpdateEntry() {
    assertThat(
            firstTraceState.toBuilder()
                .set(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .set(SECOND_KEY, FIRST_VALUE) // add a new entry
                .build())
        .asInstanceOf(type(ArrayBasedTraceState.class))
        .extracting(ArrayBasedTraceState::getEntries)
        .asList()
        .containsExactly(SECOND_KEY, FIRST_VALUE, FIRST_KEY, SECOND_VALUE);
  }

  @Test
  void addSameKey() {
    assertThat(
            EMPTY.toBuilder()
                .set(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .set(FIRST_KEY, FIRST_VALUE) // add a new entry
                .build())
        .asInstanceOf(type(ArrayBasedTraceState.class))
        .extracting(ArrayBasedTraceState::getEntries)
        .asList()
        .containsExactly(FIRST_KEY, FIRST_VALUE);
  }

  @Test
  void remove() {
    assertThat(multiValueTraceState.toBuilder().remove(SECOND_KEY).build())
        .isEqualTo(firstTraceState);
  }

  @Test
  void addAndRemoveEntry() {
    assertThat(
            EMPTY.toBuilder()
                .set(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .remove(FIRST_KEY) // add a new entry
                .build())
        .isEqualTo(EMPTY);
  }

  @Test
  void remove_NullNotAllowed() {
    assertThat(multiValueTraceState.toBuilder().remove(null).build())
        .isEqualTo(multiValueTraceState);
  }

  @Test
  void traceState_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(EMPTY, EMPTY);
    tester.addEqualityGroup(firstTraceState, EMPTY.toBuilder().set(FIRST_KEY, FIRST_VALUE).build());
    tester.addEqualityGroup(
        secondTraceState, EMPTY.toBuilder().set(SECOND_KEY, SECOND_VALUE).build());
    tester.testEquals();
  }

  @Test
  void traceState_ToString() {
    assertThat(EMPTY.toString()).isEqualTo("ArrayBasedTraceState{entries=[]}");
  }
}
