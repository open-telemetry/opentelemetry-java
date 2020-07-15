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

package io.opentelemetry.common;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AnyValueTest {

  @Test
  public void stringAnyValue() {
    String testString = "e176f922-cf82-4fe7-93d0-f68440a825cf";
    AnyValue stringValue = AnyValue.stringAnyValue(testString);
    assertThat(stringValue.getStringValue()).isEqualTo(testString);
    assertThat(stringValue.getType()).isEqualTo(AnyValue.Type.STRING);
  }

  @Test
  public void boolAnyValue() {
    AnyValue boolValue = AnyValue.boolAnyValue(true);
    assertThat(boolValue.getBoolValue()).isTrue();
    assertThat(boolValue.getType()).isEqualTo(AnyValue.Type.BOOL);
  }

  @Test
  public void intAnyValue() {
    int testInt = 12345;
    AnyValue intValue = AnyValue.intAnyValue(testInt);
    assertThat(intValue.getIntValue()).isEqualTo(testInt);
    assertThat(intValue.getType()).isEqualTo(AnyValue.Type.INT);
  }

  @Test
  public void doubleAnyValue() {
    double testDouble = 12345.0d;
    AnyValue doubleValue = AnyValue.doubleAnyValue(testDouble);
    assertThat(doubleValue.getDoubleValue()).isEqualTo(testDouble);
    assertThat(doubleValue.getType()).isEqualTo(AnyValue.Type.DOUBLE);
  }

  @Test
  public void arrayAnyValue() {
    String testString = "b729c730-4378-455b-8387-0f47250c3549";
    int testInt = 42;
    List<AnyValue> testList = new ArrayList<>();
    testList.add(AnyValue.stringAnyValue(testString));
    testList.add(AnyValue.intAnyValue(testInt));
    AnyValue arrayValue = AnyValue.arrayAnyValue(testList);
    assertThat(arrayValue.getArrayValue()).isEqualTo(testList);
    assertThat(arrayValue.getType()).isEqualTo(AnyValue.Type.ARRAY);
  }

  @Test
  public void kvlistAnyValue() {
    String testKey = "b193ffe4-b657-4e8d-8110-b77839468389";
    AnyValue testValue = AnyValue.intAnyValue(42);
    Map<String, AnyValue> testMap = new HashMap<>();
    testMap.put(testKey, testValue);
    AnyValue kvlistValue = AnyValue.kvlistAnyValue(testMap);
    assertThat(kvlistValue.getKvlistValue()).isEqualTo(testMap);
    assertThat(kvlistValue.getType()).isEqualTo(AnyValue.Type.KVLIST);
  }
}
