/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AnyValueTest {

  @Test
  void stringValue() {
    AnyValue value = AnyValue.stringAnyValue("foobar");
    assertThat(value.getStringValue()).isEqualTo("foobar");
    assertThat(value.getType()).isEqualTo(AnyValue.Type.STRING);

    assertThatThrownBy(value::getLongValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getBoolValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getDoubleValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getArrayValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getKvlistValue).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void longValue() {
    AnyValue value = AnyValue.longAnyValue(10);
    assertThat(value.getLongValue()).isEqualTo(10);
    assertThat(value.getType()).isEqualTo(AnyValue.Type.INT64);

    assertThatThrownBy(value::getStringValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getBoolValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getDoubleValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getArrayValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getKvlistValue).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void boolValue() {
    AnyValue value = AnyValue.boolAnyValue(true);
    assertThat(value.getBoolValue()).isTrue();
    assertThat(value.getType()).isEqualTo(AnyValue.Type.BOOL);

    assertThatThrownBy(value::getStringValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getLongValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getDoubleValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getArrayValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getKvlistValue).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void doubleValue() {
    AnyValue value = AnyValue.doubleAnyValue(1.0);
    assertThat(value.getDoubleValue()).isEqualTo(1.0);
    assertThat(value.getType()).isEqualTo(AnyValue.Type.DOUBLE);

    assertThatThrownBy(value::getStringValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getLongValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getBoolValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getArrayValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getKvlistValue).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void arrayValue() {
    AnyValue value =
        AnyValue.arrayAnyValue(
            Arrays.asList(AnyValue.stringAnyValue("cat"), AnyValue.stringAnyValue("dog")));
    assertThat(value.getArrayValue())
        .containsExactly(AnyValue.stringAnyValue("cat"), AnyValue.stringAnyValue("dog"));
    assertThat(value.getType()).isEqualTo(AnyValue.Type.ARRAY);

    assertThatThrownBy(value::getStringValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getLongValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getBoolValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getDoubleValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getKvlistValue).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void kvlistValue() {
    Map<String, AnyValue> map = new HashMap<>();
    map.put("animal", AnyValue.stringAnyValue("cat"));
    map.put("temperature", AnyValue.doubleAnyValue(30.0));
    AnyValue value = AnyValue.kvlistAnyValue(map);
    assertThat(value.getKvlistValue()).containsExactlyInAnyOrderEntriesOf(map);
    assertThat(value.getType()).isEqualTo(AnyValue.Type.KVLIST);

    assertThatThrownBy(value::getStringValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getLongValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getBoolValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getDoubleValue).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(value::getArrayValue).isInstanceOf(UnsupportedOperationException.class);
  }
}
