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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;

/** The extend Span interface used by the SDK. */
public interface ReadableSpan extends ExportableSpan {

  /**
   * Returns the {@link SpanContext} of the {@code Span}.
   *
   * <p>Equivalent with {@link Span#getContext()}.
   *
   * @return the {@link SpanContext} of the {@code Span}.
   * @since 0.1.0
   */
  SpanContext getSpanContext();

  /**
   * Returns the name of the {@code Span}.
   *
   * <p>The name can be changed during the lifetime of the Span by using the {@link
   * Span#updateName(String)} so this value cannot be cached.
   *
   * @return the name of the {@code Span}.
   * @since 0.1.0
   */
  String getName();

  /**
   * This method is here to convert this instance into a protobuf instance. It will be removed from
   * this class soon, so if you are writing new code you should not use this method. It is left here
   * to help reduce the number of simultaneous changes in-flight at once.
   *
   * @return a new protobuf Span instance.
   */
  io.opentelemetry.proto.trace.v1.Span toSpanProto();
}
