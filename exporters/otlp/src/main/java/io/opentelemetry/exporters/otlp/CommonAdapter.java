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

package io.opentelemetry.exporters.otlp;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue.ValueType;

final class CommonAdapter {
  static AttributeKeyValue toProtoAttribute(String key, AttributeValue attributeValue) {
    AttributeKeyValue.Builder builder = AttributeKeyValue.newBuilder().setKey(key);
    switch (attributeValue.getType()) {
      case STRING:
        return builder
            .setType(ValueType.STRING)
            .setStringValue(attributeValue.getStringValue())
            .build();
      case BOOLEAN:
        return builder
            .setType(ValueType.BOOL)
            .setBoolValue(attributeValue.getBooleanValue())
            .build();
      case LONG:
        return builder.setType(ValueType.INT).setIntValue(attributeValue.getLongValue()).build();
      case DOUBLE:
        return builder
            .setType(ValueType.DOUBLE)
            .setDoubleValue(attributeValue.getDoubleValue())
            .build();
    }
    return builder.setType(ValueType.UNRECOGNIZED).build();
  }

  private CommonAdapter() {}
}
