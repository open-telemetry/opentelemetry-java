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

import java.util.List;

@SuppressWarnings({
  "BadImport",
  "UnusedVariable",
  "PrivateConstructorForUtilityClass",
  "MultipleTopLevelClasses",
  "JavadocMethod"
})
public class Main {

  public static void main(String[] args) {
    CleanAttributes attributes =
        CleanAttributes.newBuilder()
            .setAttribute("string", "I'm a String")
            .setAttribute("boolean", true)
            .setAttribute("double", 33.444d)
            .setAttribute("long", 34333L)
            .setAttribute("stringArray", "one", "two", "three")
            .setAttribute("booleanArray", true, false, true)
            .setAttribute("longArray", 33L, 55L, 99L)
            .setAttribute("doubleArray", 123.33, 6655.33, 339393.33, 3434.33)
            .build();

    process(attributes);
  }

  public static void process(final CleanReadableAttributes attributes) {
    System.out.println("attributes = " + attributes);
    attributes.forEach(
        new CleanReadableAttributes.AttributeConsumer() {
          @Override
          public void consume(String key, AttributeValue.Type type, Object value) {
            switch (type) {
              case STRING:
                String stringValue = attributes.getStringValue(value);
                System.out.println("stringValue = " + stringValue);
                break;
              case BOOLEAN:
                Boolean booleanValue = attributes.getBooleanValue(value);
                System.out.println("booleanValue = " + booleanValue);
                break;
              case LONG:
                Long longValue = attributes.getLongValue(value);
                System.out.println("longValue = " + longValue);
                break;
              case DOUBLE:
                Double doubleValue = attributes.getDoubleValue(value);
                System.out.println("doubleValue = " + doubleValue);
                break;
              case STRING_ARRAY:
                List<String> stringArrayValue = attributes.getStringArrayValue(value);
                System.out.println("stringArrayValue = " + stringArrayValue);
                break;
              case BOOLEAN_ARRAY:
                List<Boolean> booleanArrayValue = attributes.getBooleanArrayValue(value);
                System.out.println("booleanArrayValue = " + booleanArrayValue);
                break;
              case LONG_ARRAY:
                List<Long> longArrayValue = attributes.getLongArrayValue(value);
                System.out.println("longArrayValue = " + longArrayValue);
                break;
              case DOUBLE_ARRAY:
                List<Double> doubleArrayValue = attributes.getDoubleArrayValue(value);
                System.out.println("doubleArrayValue = " + doubleArrayValue);
                break;
            }
          }
        });
  }
}
