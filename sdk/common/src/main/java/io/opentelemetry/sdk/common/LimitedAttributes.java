/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;
import io.opentelemetry.api.internal.ArrayBackedAttributes;
import io.opentelemetry.api.internal.ArrayBackedAttributesBuilder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * A mutable, limits-enforcing {@link AttributesBuilder} that also exposes a live {@link Attributes}
 * view over its current state.
 */
@SuppressWarnings("BuilderReturnThis") // Also implements Attributes; not all methods return this.
public final class LimitedAttributes extends ArrayBackedAttributesBuilder
    implements Attributes, AttributesBuilder {

  @SuppressWarnings("ExplicitArrayForVarargs") // Disambiguates against Value.of(Value<?>...)
  private static final Value<?> EMPTY_MAP_VALUE = Value.of(new KeyValue[0]);

  /** Returns a new {@link LimitedAttributes} that enforces {@code limits}. */
  public static LimitedAttributes builder(AttributeLimits limits) {
    return new LimitedAttributes(limits);
  }

  /**
   * Returns {@code attributes} with {@code limits} applied. Returns {@code attributes} itself when
   * it already fits within {@code limits} (no allocation).
   */
  public static Attributes applyLimits(AttributeLimits limits, @Nullable Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return Attributes.empty();
    }
    if (attributes.size() <= limits.getCountLimit()
        && allValuesWithinLimits(
            attributes, limits.getValueLengthLimit(), limits.getValueDepthLimit())) {
      return attributes;
    }
    LimitedAttributes limited = builder(limits);
    limited.putAll(attributes);
    return limited.build();
  }

  // Invariant: must agree with applyValueLimits on what counts as "needs truncation". See
  // applyValueLimits for details.
  private static boolean allValuesWithinLimits(
      Attributes attributes, int lengthLimit, int depthLimit) {
    if (lengthLimit == Integer.MAX_VALUE && depthLimit == Integer.MAX_VALUE) {
      return true;
    }
    if (attributes instanceof ArrayBackedAttributes) {
      // Walk the backing array directly to avoid the wrapper allocations in asMap().
      Object[] data = ((ArrayBackedAttributes) attributes).getData();
      for (int i = 1; i < data.length; i += 2) {
        if (!fitsWithinLimits(data[i], 1, lengthLimit, depthLimit)) {
          return false;
        }
      }
      return true;
    }
    // Fallback for foreign Attributes implementations.
    boolean[] fits = {true};
    attributes.forEach(
        (k, v) -> {
          if (fits[0] && !fitsWithinLimits(v, 1, lengthLimit, depthLimit)) {
            fits[0] = false;
          }
        });
    return fits[0];
  }

  private static boolean fitsWithinLimits(
      Object value, int depth, int lengthLimit, int depthLimit) {
    if (value instanceof String) {
      return lengthLimit == Integer.MAX_VALUE || ((String) value).length() <= lengthLimit;
    }
    if (value instanceof List) {
      if (depth > depthLimit) {
        return false;
      }
      if (lengthLimit == Integer.MAX_VALUE) {
        return true;
      }
      for (Object entry : (List<?>) value) {
        if (entry instanceof String && ((String) entry).length() > lengthLimit) {
          return false;
        }
      }
      return true;
    }
    if (value instanceof Value) {
      return valueFitsWithinLimits((Value<?>) value, depth, lengthLimit, depthLimit);
    }
    return true;
  }

  private static boolean valueFitsWithinLimits(
      Value<?> value, int depth, int lengthLimit, int depthLimit) {
    switch (value.getType()) {
      case STRING:
        return lengthLimit == Integer.MAX_VALUE
            || ((String) value.getValue()).length() <= lengthLimit;
      case BYTES:
        return lengthLimit == Integer.MAX_VALUE
            || ((ByteBuffer) value.getValue()).remaining() <= lengthLimit;
      case ARRAY:
        {
          if (depth > depthLimit) {
            return false;
          }
          @SuppressWarnings("unchecked")
          List<Value<?>> elements = (List<Value<?>>) value.getValue();
          int newDepth = depth + 1;
          for (Value<?> element : elements) {
            if (!valueFitsWithinLimits(element, newDepth, lengthLimit, depthLimit)) {
              return false;
            }
          }
          return true;
        }
      case KEY_VALUE_LIST:
        {
          if (depth > depthLimit) {
            return false;
          }
          @SuppressWarnings("unchecked")
          List<KeyValue> kvList = (List<KeyValue>) value.getValue();
          int newDepth = depth + 1;
          for (KeyValue kv : kvList) {
            if (!valueFitsWithinLimits(kv.getValue(), newDepth, lengthLimit, depthLimit)) {
              return false;
            }
          }
          return true;
        }
      default:
        return true;
    }
  }

  private final int countLimit;
  private final int valueLengthLimit;
  private final int valueDepthLimit;
  private int totalAddedValues;

  // Non-null entry count. Tracked separately because removeIf leaves null holes in data.
  private int size;

  // name -> index of key in {@link #data}. Lazy-init to keep empty builders allocation-free.
  // Enables O(1) amortized put; kept in sync with data by addPair / removeIf.
  @Nullable private HashMap<String, Integer> nameIndex;

  @Nullable private Attributes cachedBuild;

  LimitedAttributes(AttributeLimits limits) {
    super();
    this.countLimit = limits.getCountLimit();
    this.valueLengthLimit = limits.getValueLengthLimit();
    this.valueDepthLimit = limits.getValueDepthLimit();
  }

  /** Count of {@code put} attempts with a non-null value, including those dropped for capacity. */
  public int getTotalAddedValues() {
    return totalAddedValues;
  }

  /** Extension point invoked by every inherited {@code put}/{@code putAll} path. */
  @Override
  protected void addPair(AttributeKey<?> key, Object value) {
    totalAddedValues++;
    Object limited = applyValueLimits(value, valueLengthLimit, valueDepthLimit);
    String name = key.getKey();
    HashMap<String, Integer> index = nameIndex;
    if (index != null) {
      Integer existingIdx = index.get(name);
      if (existingIdx != null) {
        data.set(existingIdx, key);
        data.set(existingIdx + 1, limited);
        cachedBuild = null;
        return;
      }
    }
    if (size >= countLimit) {
      return;
    }
    if (index == null) {
      index = new HashMap<>();
      nameIndex = index;
    }
    int slot = data.size();
    data.add(key);
    data.add(limited);
    index.put(name, slot);
    size++;
    cachedBuild = null;
  }

  /** Overridden to decrement {@link #size} and invalidate the {@link #build()} cache. */
  @Override
  public AttributesBuilder removeIf(Predicate<AttributeKey<?>> predicate) {
    if (predicate == null) {
      return this;
    }
    HashMap<String, Integer> index = nameIndex;
    for (int i = 0; i < data.size() - 1; i += 2) {
      Object entry = data.get(i);
      if (entry instanceof AttributeKey && predicate.test((AttributeKey<?>) entry)) {
        if (index != null) {
          index.remove(((AttributeKey<?>) entry).getKey());
        }
        data.set(i, null);
        data.set(i + 1, null);
        size--;
        cachedBuild = null;
      }
    }
    return this;
  }

  @Override
  public Attributes build() {
    Attributes cached = cachedBuild;
    if (cached != null) {
      return cached;
    }
    Attributes result = super.build();
    cachedBuild = result;
    return result;
  }

  @Override
  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(AttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    HashMap<String, Integer> index = nameIndex;
    if (index == null) {
      return null;
    }
    Integer idx = index.get(key.getKey());
    if (idx == null) {
      return null;
    }
    Object storedKey = data.get(idx);
    if (storedKey == null || !storedKey.equals(key)) {
      return null;
    }
    return (T) data.get(idx + 1);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer) {
    build().forEach(consumer);
  }

  @Override
  public Map<AttributeKey<?>, Object> asMap() {
    return build().asMap();
  }

  @Override
  public AttributesBuilder toBuilder() {
    return Attributes.builder().putAll(this);
  }

  /**
   * Applies the {@code AttributeValueLengthLimit} and {@code AttributeValueDepthLimit} rules from
   * the OpenTelemetry common attribute-limits <a
   * href="https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/common#attribute-limits">specification</a>.
   * Returns {@code value} itself when nothing needs truncation.
   *
   * <p>Invariant: {@link #allValuesWithinLimits} must return {@code true} exactly when this method
   * would return every input value unchanged. The two traversals must agree on what counts as
   * "needs truncation"; otherwise the identity fast path in {@link #applyLimits} produces incorrect
   * results.
   */
  // Visible for testing
  static Object applyValueLimits(Object value, int lengthLimit, int depthLimit) {
    // Depth only applies to nested Value<> structures. For non-Value inputs, length is the only
    // knob; when length is unlimited we can return immediately.
    if (value instanceof Value) {
      if (lengthLimit == Integer.MAX_VALUE && depthLimit == Integer.MAX_VALUE) {
        return value;
      }
    } else if (lengthLimit == Integer.MAX_VALUE) {
      return value;
    }
    return applyAtDepth(value, 1, lengthLimit, depthLimit);
  }

  private static Object applyAtDepth(Object value, int depth, int lengthLimit, int depthLimit) {
    if (value instanceof String) {
      return truncateString((String) value, lengthLimit);
    }
    if (value instanceof List) {
      // Typed array; elements are always scalar so we don't recurse for depth.
      if (depth > depthLimit) {
        return Collections.emptyList();
      }
      return truncateListStrings((List<?>) value, lengthLimit);
    }
    if (value instanceof Value) {
      return applyLimitsToValue((Value<?>) value, depth, lengthLimit, depthLimit);
    }
    return value;
  }

  private static Object truncateString(String value, int lengthLimit) {
    if (value.length() <= lengthLimit) {
      return value;
    }
    return value.substring(0, lengthLimit);
  }

  // Typed array attributes only contain String / Long / Double / Boolean. Byte arrays arrive as
  // Value<BYTES> and are truncated in applyLimitsToValue.
  private static Object truncateListStrings(List<?> list, int lengthLimit) {
    if (lengthLimit == Integer.MAX_VALUE || list.isEmpty()) {
      return list;
    }
    // Two-pass so numeric-only lists (and short-string lists) return unchanged with no allocation.
    int firstChangedIndex = -1;
    int listSize = list.size();
    for (int i = 0; i < listSize; i++) {
      Object entry = list.get(i);
      if (entry instanceof String && ((String) entry).length() > lengthLimit) {
        firstChangedIndex = i;
        break;
      }
    }
    if (firstChangedIndex < 0) {
      return list;
    }
    List<Object> result = new ArrayList<>(listSize);
    for (int i = 0; i < firstChangedIndex; i++) {
      result.add(list.get(i));
    }
    for (int i = firstChangedIndex; i < listSize; i++) {
      Object entry = list.get(i);
      if (entry instanceof String && ((String) entry).length() > lengthLimit) {
        result.add(((String) entry).substring(0, lengthLimit));
      } else {
        result.add(entry);
      }
    }
    return result;
  }

  // TODO(jack-berg): convert to iterative traversal. When depth is unlimited but length is
  // limited, recursion is bounded only by the input's actual nesting depth.
  private static Value<?> applyLimitsToValue(
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
        return applyLimitsToArray(value, depth, lengthLimit, depthLimit);
      case KEY_VALUE_LIST:
        return applyLimitsToKeyValueList(value, depth, lengthLimit, depthLimit);
      default:
        return value;
    }
  }

  /** Precondition: {@code value.getType() == ARRAY}. */
  @SuppressWarnings({"unchecked", "ReferenceEquality"})
  private static Value<?> applyLimitsToArray(
      Value<?> value, int depth, int lengthLimit, int depthLimit) {
    if (depth > depthLimit) {
      return Value.of(Collections.emptyList());
    }
    List<Value<?>> elements = (List<Value<?>>) value.getValue();
    int elemCount = elements.size();
    int newDepth = depth + 1;
    Value<?>[] rewritten = null;
    for (int i = 0; i < elemCount; i++) {
      Value<?> element = elements.get(i);
      Value<?> mapped = applyLimitsToValue(element, newDepth, lengthLimit, depthLimit);
      if (rewritten != null) {
        rewritten[i] = mapped;
      } else if (mapped != element) {
        rewritten = new Value<?>[elemCount];
        for (int j = 0; j < i; j++) {
          rewritten[j] = elements.get(j);
        }
        rewritten[i] = mapped;
      }
    }
    return rewritten == null ? value : Value.of(rewritten);
  }

  /** Precondition: {@code value.getType() == KEY_VALUE_LIST}. */
  @SuppressWarnings({"unchecked", "ReferenceEquality"})
  private static Value<?> applyLimitsToKeyValueList(
      Value<?> value, int depth, int lengthLimit, int depthLimit) {
    if (depth > depthLimit) {
      return EMPTY_MAP_VALUE;
    }
    List<KeyValue> kvList = (List<KeyValue>) value.getValue();
    int kvSize = kvList.size();
    int newDepth = depth + 1;
    KeyValue[] rewritten = null;
    for (int i = 0; i < kvSize; i++) {
      KeyValue kv = kvList.get(i);
      Value<?> mapped = applyLimitsToValue(kv.getValue(), newDepth, lengthLimit, depthLimit);
      if (rewritten != null) {
        rewritten[i] = KeyValue.of(kv.getKey(), mapped);
      } else if (mapped != kv.getValue()) {
        rewritten = new KeyValue[kvSize];
        for (int j = 0; j < i; j++) {
          rewritten[j] = kvList.get(j);
        }
        rewritten[i] = KeyValue.of(kv.getKey(), mapped);
      }
    }
    return rewritten == null ? value : Value.of(rewritten);
  }
}
