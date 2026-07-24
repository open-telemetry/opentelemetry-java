/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.context.propagation.internal.ExtendedTextMapGetter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class TextMapGetterTest {

  final TextMapGetter<Void> nullGet =
      new ExtendedTextMapGetter<Void>() {
        @Override
        public Iterable<String> keys(Void carrier) {
          return ImmutableList.of("key");
        }

        @Nullable
        @Override
        public String get(@Nullable Void carrier, String key) {
          return null;
        }
      };

  final TextMapGetter<Void> nonNullGet =
      new ExtendedTextMapGetter<Void>() {
        @Override
        public Iterable<String> keys(Void carrier) {
          return ImmutableList.of("key");
        }

        @Override
        public String get(@Nullable Void carrier, String key) {
          return "123";
        }
      };

  @Test
  void canBeImplementedAsLambda() {
    TextMapGetter<Void> getter = (carrier, key) -> "value";

    assertThat(getter.get(null, "key")).isEqualTo("value");
    assertThatThrownBy(() -> getter.keys(null))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("keys() is not implemented");
  }

  @Test
  void extendedTextMapGetterdefaultMethod_returnsEmpty() {
    Iterator<String> result = nullGet.getAll(null, "key");
    assertThat(result).isNotNull();
    List<String> values = iterToList(result);
    assertThat(values).isEqualTo(Collections.emptyList());
  }

  @Test
  void extendedTextMapGetterdefaultMethod_returnsSingleVal() {
    Iterator<String> result = nonNullGet.getAll(null, "key");
    assertThat(result).isNotNull();
    List<String> values = iterToList(result);
    assertThat(values).isEqualTo(Collections.singletonList("123"));
  }

  private static <T> List<T> iterToList(Iterator<T> iter) {
    List<T> list = new ArrayList<>();
    while (iter.hasNext()) {
      list.add(iter.next());
    }
    return list;
  }
}
