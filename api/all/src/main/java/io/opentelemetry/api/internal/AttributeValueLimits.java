/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Applies the {@code AttributeValueLengthLimit} and {@code AttributeValueDepthLimit} rules from the
 * OpenTelemetry common attribute-limits <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/common#attribute-limits">specification</a>
 * to a single attribute value.
 *
 * <p>Both passes are performed in a single traversal. Values that don't require modification pass
 * through by reference; no allocation is performed on the pure fast path.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AttributeValueLimits {

  @SuppressWarnings("ExplicitArrayForVarargs") // Disambiguates against Value.of(Value<?>...)
  private static final Value<?> EMPTY_MAP_VALUE = Value.of(new KeyValue[0]);

  private AttributeValueLimits() {}

  /**
   * Returns {@code value} truncated to {@code lengthLimit} and depth-limited to {@code depthLimit},
   * or {@code value} itself if no changes are needed.
   */
  public static Object apply(Object value, int lengthLimit, int depthLimit) {
    if (lengthLimit == Integer.MAX_VALUE && depthLimit == Integer.MAX_VALUE) {
      return value;
    }
    return applyAtDepth(value, 1, lengthLimit, depthLimit);
  }

  /**
   * Equivalent to {@link #apply(Object, int, int) apply(value, lengthLimit, Integer.MAX_VALUE)}.
   */
  public static Object applyLengthLimit(Object value, int lengthLimit) {
    return apply(value, lengthLimit, Integer.MAX_VALUE);
  }

  /**
   * Returns true if {@code value} would be unchanged by {@link #applyLengthLimit(Object, int)} with
   * the same limit.
   */
  @SuppressWarnings("ReferenceEquality")
  public static boolean isValidLength(Object value, int lengthLimit) {
    if (lengthLimit == Integer.MAX_VALUE) {
      return true;
    }
    return apply(value, lengthLimit, Integer.MAX_VALUE) == value;
  }

  private static Object applyAtDepth(Object value, int depth, int lengthLimit, int depthLimit) {
    if (value instanceof String) {
      return applyStringLength((String) value, lengthLimit);
    }
    if (value instanceof List) {
      // Typed array; elements are always scalar so we don't recurse for depth.
      if (depth > depthLimit) {
        return emptyTypedList((List<?>) value);
      }
      return applyListLength((List<?>) value, lengthLimit);
    }
    if (value instanceof Value) {
      return applyValueAtDepth((Value<?>) value, depth, lengthLimit, depthLimit);
    }
    return value;
  }

  private static Object applyStringLength(String value, int lengthLimit) {
    if (lengthLimit == Integer.MAX_VALUE || value.length() <= lengthLimit) {
      return value;
    }
    return value.substring(0, lengthLimit);
  }

  private static Object applyListLength(List<?> list, int lengthLimit) {
    if (lengthLimit == Integer.MAX_VALUE || list.isEmpty()) {
      return list;
    }
    // Two-pass so numeric-only lists (and short-string lists) return unchanged with no allocation.
    int firstChangedIndex = -1;
    int size = list.size();
    for (int i = 0; i < size; i++) {
      Object entry = list.get(i);
      if (entry instanceof String && ((String) entry).length() > lengthLimit) {
        firstChangedIndex = i;
        break;
      }
    }
    if (firstChangedIndex < 0) {
      return list;
    }
    List<Object> result = new ArrayList<>(size);
    for (int i = 0; i < firstChangedIndex; i++) {
      result.add(list.get(i));
    }
    for (int i = firstChangedIndex; i < size; i++) {
      Object entry = list.get(i);
      if (entry instanceof String && ((String) entry).length() > lengthLimit) {
        result.add(((String) entry).substring(0, lengthLimit));
      } else {
        result.add(entry);
      }
    }
    return result;
  }

  private static Value<?> applyValueAtDepth(
      Value<?> value, int depth, int lengthLimit, int depthLimit) {
    ValueType type = value.getType();
    switch (type) {
      case STRING:
        {
          if (lengthLimit == Integer.MAX_VALUE) {
            return value;
          }
          String str = (String) value.getValue();
          if (str.length() <= lengthLimit) {
            return value;
          }
          return Value.of(str.substring(0, lengthLimit));
        }
      case BYTES:
        {
          if (lengthLimit == Integer.MAX_VALUE) {
            return value;
          }
          ByteBuffer buffer = (ByteBuffer) value.getValue();
          if (buffer.remaining() <= lengthLimit) {
            return value;
          }
          byte[] truncated = new byte[lengthLimit];
          buffer.get(truncated);
          return Value.of(truncated);
        }
      case ARRAY:
        return applyArrayAtDepth(value, depth, lengthLimit, depthLimit);
      case KEY_VALUE_LIST:
        return applyKeyValueListAtDepth(value, depth, lengthLimit, depthLimit);
      default:
        return value;
    }
  }

  @SuppressWarnings({"unchecked", "ReferenceEquality"})
  private static Value<?> applyArrayAtDepth(
      Value<?> value, int depth, int lengthLimit, int depthLimit) {
    if (depth > depthLimit) {
      return Value.of(Collections.<Value<?>>emptyList());
    }
    List<Value<?>> elements = (List<Value<?>>) value.getValue();
    int size = elements.size();
    int newDepth = depth + 1;
    Value<?>[] rewritten = null;
    for (int i = 0; i < size; i++) {
      Value<?> element = elements.get(i);
      Value<?> mapped = applyValueAtDepth(element, newDepth, lengthLimit, depthLimit);
      if (rewritten != null) {
        rewritten[i] = mapped;
      } else if (mapped != element) {
        rewritten = new Value<?>[size];
        for (int j = 0; j < i; j++) {
          rewritten[j] = elements.get(j);
        }
        rewritten[i] = mapped;
      }
    }
    return rewritten == null ? value : Value.of(rewritten);
  }

  @SuppressWarnings({"unchecked", "ReferenceEquality"})
  private static Value<?> applyKeyValueListAtDepth(
      Value<?> value, int depth, int lengthLimit, int depthLimit) {
    if (depth > depthLimit) {
      return EMPTY_MAP_VALUE;
    }
    List<KeyValue> kvList = (List<KeyValue>) value.getValue();
    int size = kvList.size();
    int newDepth = depth + 1;
    KeyValue[] rewritten = null;
    for (int i = 0; i < size; i++) {
      KeyValue kv = kvList.get(i);
      Value<?> mapped = applyValueAtDepth(kv.getValue(), newDepth, lengthLimit, depthLimit);
      if (rewritten != null) {
        rewritten[i] = KeyValue.of(kv.getKey(), mapped);
      } else if (mapped != kv.getValue()) {
        rewritten = new KeyValue[size];
        for (int j = 0; j < i; j++) {
          rewritten[j] = kvList.get(j);
        }
        rewritten[i] = KeyValue.of(kv.getKey(), mapped);
      }
    }
    return rewritten == null ? value : Value.of(rewritten);
  }

  private static List<?> emptyTypedList(List<?> list) {
    if (list.isEmpty()) {
      return list;
    }
    return Collections.emptyList();
  }
}
