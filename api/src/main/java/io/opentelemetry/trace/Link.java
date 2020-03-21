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

package io.opentelemetry.trace;

import io.opentelemetry.common.AttributeValue;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A link to a {@link Span}.
 *
 * <p>Used (for example) in batching operations, where a single batch handler processes multiple
 * requests from different traces. Link can be also used to reference spans from the same trace.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface Link {
  /**
   * Returns the {@code SpanContext}.
   *
   * @return the {@code SpanContext}.
   * @since 0.1.0
   */
  SpanContext getContext();

  /**
   * Returns the set of attributes.
   *
   * @return the set of attributes.
   * @since 0.1.0
   */
  Map<String, AttributeValue> getAttributes();
}
