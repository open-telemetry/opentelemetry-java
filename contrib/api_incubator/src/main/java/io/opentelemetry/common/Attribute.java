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

public class Attribute {
  private final AttributeKey key;
  private final Object value;

  private Attribute(AttributeKey key, Object value) {
    this.key = key;
    this.value = value;
  }

  public static Attribute create(BooleanValuedKey key, boolean value) {
    return new Attribute(key, value);
  }

  public static Attribute create(LongValuedKey key, long value) {
    return new Attribute(key, value);
  }

  public static Attribute create(DoubleValuedKey key, double value) {
    return new Attribute(key, value);
  }

  public static Attribute create(StringValuedKey key, String value) {
    return new Attribute(key, value);
  }

  public AttributeKey key() {
    return key;
  }

  public boolean getBooleanValue() {
    return (boolean) value;
  }

  public long getLongValue() {
    return (long) value;
  }

  public double getDoubleValue() {
    return (double) value;
  }

  public String getStringValue() {
    return (String) value;
  }
}
