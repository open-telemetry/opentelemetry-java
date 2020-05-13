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

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.SpanContext;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable implementation of {@link Link}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class ResolvedLink implements Link {

  private static final Map<String, AttributeValue> DEFAULT_ATTRIBUTE_COLLECTION =
      Collections.emptyMap();
  private static final int DEFAULT_ATTRIBUTE_COUNT = 0;

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @return a new immutable {@code TimedEvent<T>}
   * @since 0.1.0
   */
  public static ResolvedLink create(SpanContext spanContext) {
    return new AutoValue_ResolvedLink(
        spanContext, DEFAULT_ATTRIBUTE_COLLECTION, DEFAULT_ATTRIBUTE_COUNT);
  }

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @param attributes the attributes of this {@code Link}.
   * @return a new immutable {@code TimedEvent<T>}
   * @since 0.1.0
   */
  public static ResolvedLink create(
      SpanContext spanContext, Map<String, AttributeValue> attributes) {
    return new AutoValue_ResolvedLink(
        spanContext,
        Collections.unmodifiableMap(new LinkedHashMap<>(attributes)),
        attributes.size());
  }

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @param attributes the attributes of this {@code Link}.
   * @param totalAttributeCount the total number of attributed for this {@code Link}.
   * @return a new immutable {@code TimedEvent<T>}
   * @since 0.1.0
   */
  public static ResolvedLink create(
      SpanContext spanContext, Map<String, AttributeValue> attributes, int totalAttributeCount) {
    return new AutoValue_ResolvedLink(
        spanContext,
        Collections.unmodifiableMap(new LinkedHashMap<>(attributes)),
        totalAttributeCount);
  }

  /**
   * The total number of attributes that were recorded on this Link. This number may be larger than
   * the number of attributes that are attached to this span, if the total number recorded was
   * greater than the configured maximum value. See: {@link
   * TraceConfig#getMaxNumberOfAttributesPerLink()}
   *
   * @return The number of attributes on this link.
   */
  public abstract int getTotalAttributeCount();

  ResolvedLink() {}
}
