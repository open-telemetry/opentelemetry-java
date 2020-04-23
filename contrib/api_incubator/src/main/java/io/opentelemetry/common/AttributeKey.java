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

public interface AttributeKey {
  enum Type {
    BOOLEAN,
    LONG,
    DOUBLE,
    STRING
  }

  Type getType();

  String key();

  @AutoValue
  @Immutable
  abstract class BooleanValuedKey implements AttributeKey {

    public static BooleanValuedKey create(String key) {
      return new AutoValue_AttributeKey_BooleanValuedKey(key);
    }

    @Override
    public Type getType() {
      return Type.BOOLEAN;
    }
  }

  @AutoValue
  @Immutable
  abstract class DoubleValuedKey implements AttributeKey {
    public static DoubleValuedKey create(String key) {
      return new AutoValue_AttributeKey_DoubleValuedKey(key);
    }

    @Override
    public Type getType() {
      return Type.DOUBLE;
    }
  }

  @AutoValue
  @Immutable
  abstract class LongValuedKey implements AttributeKey {
    public static LongValuedKey create(String key) {
      return new AutoValue_AttributeKey_LongValuedKey(key);
    }

    @Override
    public Type getType() {
      return Type.LONG;
    }
  }

  @AutoValue
  @Immutable
  abstract class StringValuedKey implements AttributeKey {
    public static StringValuedKey create(String key) {
      return new AutoValue_AttributeKey_StringValuedKey(key);
    }

    @Override
    public Type getType() {
      return Type.STRING;
    }
  }
}
