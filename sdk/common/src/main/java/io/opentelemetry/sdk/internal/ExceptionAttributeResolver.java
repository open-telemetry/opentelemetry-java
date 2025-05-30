/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.AttributeKey;
import javax.annotation.Nullable;

/**
 * Implementations resolve {@code exception.*} attributes attached to span events, logs, etc.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public interface ExceptionAttributeResolver {

  AttributeKey<String> EXCEPTION_TYPE = AttributeKey.stringKey("exception.type");
  AttributeKey<String> EXCEPTION_MESSAGE = AttributeKey.stringKey("exception.message");
  AttributeKey<String> EXCEPTION_STACKTRACE = AttributeKey.stringKey("exception.stacktrace");

  void setExceptionAttributes(
      AttributeSetter attributeSetter, Throwable throwable, int maxAttributeLength);

  /**
   * This class is internal and experimental. Its APIs are unstable and can change at any time. Its
   * APIs (or a version of them) may be promoted to the public stable API in the future, but no
   * guarantees are made.
   */
  // TODO(jack-berg): Consider promoting to opentelemetry and extending with Span, LogRecordBuilder,
  // AttributeBuilder, AttributesMap etc.
  interface AttributeSetter {
    <T> void setAttribute(AttributeKey<T> key, @Nullable T value);
  }
}
