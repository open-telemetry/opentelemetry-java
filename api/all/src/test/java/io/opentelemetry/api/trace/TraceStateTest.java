/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class TraceStateTest {
  private static final String FIRST_KEY = "key_1";
  private static final String SECOND_KEY = "key_2";
  private static final String FIRST_VALUE = "value.1";
  private static final String SECOND_VALUE = "value.2";

  private final TraceState firstTraceState =
      TraceState.builder().put(FIRST_KEY, FIRST_VALUE).build();
  private final TraceState secondTraceState =
      TraceState.builder().put(SECOND_KEY, SECOND_VALUE).build();
  private final TraceState multiValueTraceState =
      TraceState.builder().put(FIRST_KEY, FIRST_VALUE).put(SECOND_KEY, SECOND_VALUE).build();

  @Test
  void get() {
    assertThat(firstTraceState.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(secondTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(multiValueTraceState.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(multiValueTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(firstTraceState.get("dog")).isNull();
    assertThat(firstTraceState.get(null)).isNull();
  }

  @Test
  void sizeAndEmpty() {
    assertThat(TraceState.getDefault().size()).isZero();
    assertThat(TraceState.getDefault().isEmpty()).isTrue();

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

    assertThatCode(() -> firstTraceState.forEach(null)).doesNotThrowAnyException();
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
    assertThat(TraceState.builder().put(null, FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void disallowsEmptyKey() {
    assertThat(TraceState.builder().put("", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void invalidFirstKeyCharacter() {
    assertThat(TraceState.builder().put("$_key", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void firstKeyCharacterDigitIsAllowed() {
    // note: a digit is only allowed if the key is in the tenant format (with an '@')
    TraceState result = TraceState.builder().put("1@tenant", FIRST_VALUE).build();
    assertThat(result.get("1@tenant")).isEqualTo(FIRST_VALUE);
  }

  @Test
  void testValidLongTenantId() {
    TraceState result = TraceState.builder().put("12345678901234567890@nr", FIRST_VALUE).build();
    assertThat(result.get("12345678901234567890@nr")).isEqualTo(FIRST_VALUE);
  }

  @Test
  void invalidKeyCharacters() {
    assertThat(TraceState.builder().put("kEy_1", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void testValidAtSignVendorNamePrefix() {
    TraceState result = TraceState.builder().put("1@nr", FIRST_VALUE).build();
    assertThat(result.get("1@nr")).isEqualTo(FIRST_VALUE);
  }

  @Test
  void testVendorIdWith14Characters() {
    String key = "1@nrabcdefghijkl";
    assertThat(TraceState.builder().put(key, FIRST_VALUE).build().get(key)).isEqualTo(FIRST_VALUE);
  }

  @Test
  void testVendorIdLongerThan14Characters() {
    assertThat(TraceState.builder().put("1@nrabcdefghijklm", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void testVendorIdLongerThan14Characters_longTenantId() {
    assertThat(
            TraceState.builder().put("12345678901234567890@nrabcdefghijklm", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void tenantIdWith241Characters() {
    char[] chars = new char[241];
    Arrays.fill(chars, 'a');
    String tenantId = new String(chars);
    assertThat(
            TraceState.builder().put(tenantId + "@nr", FIRST_VALUE).build().get(tenantId + "@nr"))
        .isEqualTo(FIRST_VALUE);
  }

  @Test
  void tenantIdLongerThan241Characters() {
    char[] chars = new char[242];
    Arrays.fill(chars, 'a');
    String tenantId = new String(chars);
    assertThat(TraceState.builder().put(tenantId + "@nr", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void testNonVendorFormatFirstKeyCharacter() {
    assertThat(TraceState.builder().put("1acdfrgs", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void testMultipleAtSignNotAllowed() {
    assertThat(TraceState.builder().put("1@n@r@", FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void invalidKeySize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longKey = new String(chars);
    assertThat(TraceState.builder().put(longKey, FIRST_VALUE).build())
        .isEqualTo(TraceState.getDefault());
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
    assertThat(TraceState.builder().put(allowedKey, FIRST_VALUE).build().get(allowedKey))
        .isEqualTo(FIRST_VALUE);
  }

  @Test
  void invalidValueSize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longValue = new String(chars);
    assertThat(TraceState.builder().put(FIRST_KEY, longValue).build())
        .isEqualTo(TraceState.getDefault());
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
    assertThat(TraceState.builder().put(FIRST_KEY, allowedValue).build().get(FIRST_KEY))
        .isEqualTo(allowedValue);
  }

  @Test
  @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
  void invalidValues() {
    assertThat(TraceState.builder().put(FIRST_KEY, null).build())
        .isEqualTo(TraceState.getDefault());
    assertThat(TraceState.builder().put("foo", "bar,").build()).isEqualTo(TraceState.getDefault());
    assertThat(TraceState.builder().put("foo", "bar ").build()).isEqualTo(TraceState.getDefault());
    assertThat(TraceState.builder().put("foo", "bar=").build()).isEqualTo(TraceState.getDefault());
    assertThat(TraceState.builder().put("foo", "bar\u0019").build())
        .isEqualTo(TraceState.getDefault());
    assertThat(TraceState.builder().put("foo", "bar\u007F").build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void addEntry() {
    assertThat(firstTraceState.toBuilder().put(SECOND_KEY, SECOND_VALUE).build())
        .isEqualTo(multiValueTraceState);
  }

  @Test
  void updateEntry() {
    assertThat(firstTraceState.toBuilder().put(FIRST_KEY, SECOND_VALUE).build().get(FIRST_KEY))
        .isEqualTo(SECOND_VALUE);
    TraceState updatedMultiValueTraceState =
        multiValueTraceState.toBuilder().put(FIRST_KEY, SECOND_VALUE).build();
    assertThat(updatedMultiValueTraceState.get(FIRST_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(updatedMultiValueTraceState.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
  }

  @Test
  void addAndUpdateEntry() {
    assertThat(
            firstTraceState.toBuilder()
                .put(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .put(SECOND_KEY, FIRST_VALUE) // add a new entry
                .build()
                .asMap())
        .containsExactly(entry(SECOND_KEY, FIRST_VALUE), entry(FIRST_KEY, SECOND_VALUE));
  }

  @Test
  void addSameKey() {
    assertThat(
            TraceState.builder()
                .put(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .put(FIRST_KEY, FIRST_VALUE) // add a new entry
                .build()
                .asMap())
        .containsExactly(entry(FIRST_KEY, FIRST_VALUE));
  }

  @Test
  void tooManyEntries() {
    TraceStateBuilder stateBuilder = TraceState.builder();
    for (int i = 0; i < 32; i++) {
      stateBuilder.put("key" + i, "val");
      // Make sure removal does actually allow adding more keys up to the limit.
      stateBuilder.remove("key" + i);
      stateBuilder.put("key" + i, "val");
    }
    stateBuilder.put("key32", "val");
    TraceState state = stateBuilder.build();
    assertThat(state.size()).isEqualTo(32);
    for (int i = 0; i < 32; i++) {
      assertThat(state.get("key" + i)).isEqualTo("val");
    }
  }

  @Test
  void remove() {
    assertThat(multiValueTraceState.toBuilder().remove(SECOND_KEY).build())
        .isEqualTo(firstTraceState);
  }

  @Test
  void removeNotPresent() {
    assertThat(multiValueTraceState.toBuilder().remove("unknown").build())
        .isEqualTo(multiValueTraceState);
  }

  @Test
  void addAndRemoveEntry() {
    assertThat(
            TraceState.builder()
                .put(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .remove(FIRST_KEY) // add a new entry
                .build())
        .isEqualTo(TraceState.getDefault());
  }

  @Test
  void addRemoveAndAddAgainEntry() {
    assertThat(
            TraceState.builder()
                .put(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .remove(FIRST_KEY) // add a new entry
                .put(FIRST_KEY, FIRST_VALUE)
                .build()
                .asMap())
        .containsExactly(entry(FIRST_KEY, FIRST_VALUE));
  }

  @Test
  void remove_NullNotAllowed() {
    assertThat(multiValueTraceState.toBuilder().remove(null).build())
        .isEqualTo(multiValueTraceState);
  }

  @Test
  void traceState_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        TraceState.getDefault(),
        TraceState.getDefault(),
        TraceState.getDefault().toBuilder().build(),
        TraceState.builder().build());
    tester.addEqualityGroup(
        firstTraceState, TraceState.builder().put(FIRST_KEY, FIRST_VALUE).build());
    tester.addEqualityGroup(
        secondTraceState, TraceState.builder().put(SECOND_KEY, SECOND_VALUE).build());
    tester.testEquals();
  }

  @Test
  void traceState_ToString() {
    assertThat(TraceState.getDefault().toString()).isEqualTo("ArrayBasedTraceState{entries=[]}");
  }

  @Test
  void doesNotCrash() {
    assertThat(TraceState.getDefault().get(null)).isNull();
    assertThatCode(() -> TraceState.getDefault().forEach(null)).doesNotThrowAnyException();
  }

  @Test
  void reuseBuilder() {
    TraceStateBuilder builder = TraceState.builder();
    builder.put("animal", "bear");
    TraceState state1 = builder.build();
    builder.put("food", "pizza");
    TraceState state2 = builder.build();
    assertThat(state1.asMap()).containsExactly(entry("animal", "bear"));
    assertThat(state2.asMap()).containsExactly(entry("food", "pizza"), entry("animal", "bear"));
  }

  // Not strictly a necessary test for behavior but it's still good to verify optimizations hold in
  // case things get refactored.
  @Test
  void emptyIsSingleton() {
    assertThat(TraceState.builder().build()).isSameAs(TraceState.getDefault());
    assertThat(TraceState.builder().put("animal", "bear").remove("animal").build())
        .isSameAs(TraceState.getDefault());
    assertThat(
            TraceState.builder()
                .put("animal", "bear")
                .put("food", "pizza")
                .remove("animal")
                .remove("food")
                .build())
        .isSameAs(TraceState.getDefault());
  }
}
