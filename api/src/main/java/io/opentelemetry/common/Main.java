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

import io.opentelemetry.common.CleanReadableAttributes.RawAttributeConsumer;
import io.opentelemetry.common.CleanReadableAttributes.TypedAttributeConsumer;
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
            .setString("string", "I'm a String")
            .setBoolean("boolean", true)
            .setDouble("double", 33.444d)
            .setLong("long", 34333L)
            .setStringArray("stringArray", "one", "two", "three")
            .setBooleanArray("booleanArray", true, false, true)
            .setLongArray("longArray", 33L, 55L, 99L)
            .setDoubleArray("doubleArray", 123.33, 6655.33, 339393.33, 3434.33)
            .build();

    System.out.println("attributes = " + attributes);
    System.out.println();
    System.out.println("Processing with casts:");
    processRaw(attributes);

    System.out.println();
    System.out.println("Processing with types:");
    processTyped(attributes);
  }

  public static void processRaw(CleanReadableAttributes attributes) {
    attributes.forEach(
        new RawAttributeConsumer() {
          @SuppressWarnings("unchecked")
          @Override
          public void consume(String key, AttributeType type, Object value) {
            switch (type) {
              case STRING:
                String stringValue = (String) value;
                System.out.println("stringValue = " + stringValue);
                break;
              case BOOLEAN:
                boolean booleanValue = (boolean) value;
                System.out.println("booleanValue = " + booleanValue);
                break;
              case LONG:
                long longValue = (long) value;
                System.out.println("longValue = " + longValue);
                break;
              case DOUBLE:
                double doubleValue = (double) value;
                System.out.println("doubleValue = " + doubleValue);
                break;
              case STRING_ARRAY:
                List<String> stringArrayValue = (List<String>) value;
                System.out.println("stringArrayValue = " + stringArrayValue);
                break;
              case BOOLEAN_ARRAY:
                List<Boolean> booleanArrayValue = (List<Boolean>) value;
                System.out.println("booleanArrayValue = " + booleanArrayValue);
                break;
              case LONG_ARRAY:
                List<Long> longArrayValue = (List<Long>) value;
                System.out.println("longArrayValue = " + longArrayValue);
                break;
              case DOUBLE_ARRAY:
                List<Double> doubleArrayValue = (List<Double>) value;
                System.out.println("doubleArrayValue = " + doubleArrayValue);
                break;
            }
          }
        });
  }

  private static void processTyped(CleanAttributes attributes) {
    attributes.forEach(
        new TypedAttributeConsumer() {
          @Override
          public void consumeString(String key, String value) {
            System.out.println(key + " = " + value);
          }

          @Override
          public void consumeLong(String key, long value) {
            System.out.println(key + " = " + value);
          }

          @Override
          public void consumeDouble(String key, double value) {
            System.out.println(key + " = " + value);
          }

          @Override
          public void consumeBoolean(String key, boolean value) {
            System.out.println(key + " = " + value);
          }

          @Override
          public void consumeStringArray(String key, List<String> value) {
            System.out.println(key + " = " + value);
          }

          @Override
          public void consumeLongArray(String key, List<Long> value) {
            System.out.println(key + " = " + value);
          }

          @Override
          public void consumeDoubleArray(String key, List<Double> value) {
            System.out.println(key + " = " + value);
          }

          @Override
          public void consumeBooleanArray(String key, List<Boolean> value) {
            System.out.println(key + " = " + value);
          }
        });
  }
}
