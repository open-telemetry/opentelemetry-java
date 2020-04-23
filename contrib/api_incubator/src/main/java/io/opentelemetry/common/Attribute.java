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

import com.google.auto.value.AutoValue;
import jdk.nashorn.internal.ir.annotations.Immutable;

@AutoValue
@Immutable
public abstract class Attribute {

  public abstract AttributeKey key();

  abstract Object value();

  public boolean getBooleanValue() {
    return (boolean) value();
  }

  public long getLongValue() {
    return (long) value();
  }

  public double getDoubleValue() {
    return (double) value();
  }

  public String getStringValue() {
    return (String) value();
  }

  public static Attribute create(AttributeKey.BooleanValuedKey key, boolean value) {
    return new AutoValue_Attribute(key, value);
  }

  public static Attribute create(AttributeKey.LongValuedKey key, long value) {
    return new AutoValue_Attribute(key, value);
  }

  public static Attribute create(AttributeKey.DoubleValuedKey key, double value) {
    return new AutoValue_Attribute(key, value);
  }

  public static Attribute create(AttributeKey.StringValuedKey key, String value) {
    return new AutoValue_Attribute(key, value);
  }
}
