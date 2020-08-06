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

@SuppressWarnings("unchecked")
public enum AttributeType {
  STRING,
  BOOLEAN,
  LONG,
  DOUBLE,
  STRING_ARRAY,
  BOOLEAN_ARRAY,
  LONG_ARRAY,
  DOUBLE_ARRAY;

  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T> T cast(Object value) {
    return (T) value;
  }

  public String asString(Object value) {
    return (String) value;
  }

  public long asLong(Object value) {
    return (long) value;
  }

  public double asDouble(Object value) {
    return (double) value;
  }

  public boolean asBoolean(Object value) {
    return (boolean) value;
  }

  public List<String> asStringArray(Object value) {
    return (List<String>) value;
  }

  public List<Long> asLongArray(Object value) {
    return (List<Long>) value;
  }

  public List<Double> asDoubleArray(Object value) {
    return (List<Double>) value;
  }

  public List<Boolean> asBooleanArray(Object value) {
    return (List<Boolean>) value;
  }
}
