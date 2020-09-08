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

class AttributeKeyImpl<T> implements AttributeKey<T> {
  private final String key;

  AttributeKeyImpl(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }

  static class StringKey extends AttributeKeyImpl<String> {
    public StringKey(String key) {
      super(key);
    }
  }

  static class BooleanKey extends AttributeKeyImpl<Boolean> {
    BooleanKey(String key) {
      super(key);
    }
  }

  static class LongKey extends AttributeKeyImpl<Long> {
    LongKey(String key) {
      super(key);
    }
  }

  static class DoubleKey extends AttributeKeyImpl<Double> {
    DoubleKey(String key) {
      super(key);
    }
  }

  static class StringArrayKey extends AttributeKeyImpl<List<String>> {
    public StringArrayKey(String key) {
      super(key);
    }
  }

  static class BooleanArrayKey extends AttributeKeyImpl<List<Boolean>> {
    BooleanArrayKey(String key) {
      super(key);
    }
  }

  static class LongArrayKey extends AttributeKeyImpl<List<Long>> {
    LongArrayKey(String key) {
      super(key);
    }
  }

  static class DoubleArrayKey extends AttributeKeyImpl<List<Double>> {
    DoubleArrayKey(String key) {
      super(key);
    }
  }
}
