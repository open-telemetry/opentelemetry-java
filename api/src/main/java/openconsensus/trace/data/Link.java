/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace.data;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import openconsensus.trace.Span;
import openconsensus.trace.SpanContext;

/**
 * A link to a {@link Span} from a different trace.
 *
 * <p>It requires a {@link Type} which describes the relationship with the linked {@code Span} and
 * the identifiers of the linked {@code Span}.
 *
 * <p>Used (for example) in batching operations, where a single batch handler processes multiple
 * requests from different traces.
 *
 * @since 0.1
 */
@Immutable
@AutoValue
public abstract class Link {
  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES = Collections.emptyMap();

  /**
   * The relationship with the linked {@code Span} relative to the current {@code Span}.
   *
   * @since 0.1
   */
  public enum Type {
    /**
     * When the linked {@code Span} is a child of the current {@code Span}.
     *
     * @since 0.1
     */
    CHILD_LINKED_SPAN,
    /**
     * When the linked {@code Span} is a parent of the current {@code Span}.
     *
     * @since 0.1
     */
    PARENT_LINKED_SPAN
  }

  /**
   * Returns a new {@code Link}.
   *
   * @param context the context of the linked {@code Span}.
   * @param type the type of the relationship with the linked {@code Span}.
   * @return a new {@code Link}.
   * @since 0.1
   */
  public static Link fromSpanContext(SpanContext context, Type type) {
    return new AutoValue_Link(context.getTraceId(), context.getSpanId(), type, EMPTY_ATTRIBUTES);
  }

  /**
   * Returns a new {@code Link}.
   *
   * @param context the context of the linked {@code Span}.
   * @param type the type of the relationship with the linked {@code Span}.
   * @param attributes the attributes of the {@code Link}.
   * @return a new {@code Link}.
   * @since 0.1
   */
  public static Link fromSpanContext(
      SpanContext context, Type type, Map<String, AttributeValue> attributes) {
    return new AutoValue_Link(
        context.getTraceId(),
        context.getSpanId(),
        type,
        Collections.unmodifiableMap(new HashMap<String, AttributeValue>(attributes)));
  }

  /**
   * Returns the {@code TraceId}.
   *
   * @return the {@code TraceId}.
   * @since 0.1
   */
  public abstract TraceId getTraceId();

  /**
   * Returns the {@code SpanId}.
   *
   * @return the {@code SpanId}
   * @since 0.1
   */
  public abstract SpanId getSpanId();

  /**
   * Returns the {@code Type}.
   *
   * @return the {@code Type}.
   * @since 0.1
   */
  public abstract Type getType();

  /**
   * Returns the set of attributes.
   *
   * @return the set of attributes.
   * @since 0.1
   */
  public abstract Map<String, AttributeValue> getAttributes();

  Link() {}
}
