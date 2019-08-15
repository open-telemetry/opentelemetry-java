/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.trace.util;

import com.google.auto.value.AutoValue;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.SpanContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * Static class to create {@link Link} objects.
 *
 * @since 0.1.0
 */
public final class Links {
  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES = Collections.emptyMap();

  /**
   * Returns a new {@code Link}.
   *
   * @param context the context of the linked {@code Span}.
   * @return a new {@code Link}.
   * @since 0.1.0
   */
  public static Link create(SpanContext context) {
    return new AutoValue_Links_SimpleLink(context, EMPTY_ATTRIBUTES);
  }

  /**
   * Returns a new {@code Link}.
   *
   * @param context the context of the linked {@code Span}.
   * @param attributes the attributes of the {@code Link}.
   * @return a new {@code Link}.
   * @since 0.1.0
   */
  public static Link create(SpanContext context, Map<String, AttributeValue> attributes) {
    return new AutoValue_Links_SimpleLink(
        context, Collections.unmodifiableMap(new HashMap<>(attributes)));
  }

  /**
   * An immutable implementation of {@link Link}.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  abstract static class SimpleLink implements Link {}

  private Links() {}
}
