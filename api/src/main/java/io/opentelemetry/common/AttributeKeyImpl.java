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

@SuppressWarnings("rawtypes")
@AutoValue
abstract class AttributeKeyImpl<T> implements AttributeKey<T> {

  static <T> AttributeKeyImpl<T> create(String key, AttributeType type) {
    return new AutoValue_AttributeKeyImpl<>(key, type);
  }

  //////////////////////////////////
  // IMPORTANT: the equals/hashcode/compareTo *only* include the key, and not the type,
  // so that de-duping of attributes is based on the key, and not also based on the type.
  //////////////////////////////////

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributeKeyImpl)) {
      return false;
    }

    AttributeKeyImpl<?> that = (AttributeKeyImpl<?>) o;

    return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;
  }

  @Override
  public final int hashCode() {
    return getKey() != null ? getKey().hashCode() : 0;
  }

  @Override
  public int compareTo(AttributeKey o) {
    return getKey().compareTo(o.getKey());
  }
}
