/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static io.opentelemetry.sdk.common.internal.DefaultExceptionAttributeResolver.ENABLE_JVM_STACKTRACE_PROPERTY;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.internal.ConfigUtil;
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
   * Return the default exception attribute resolver, setting {@code jvmStacktraceEnabled} based on
   * {@link DefaultExceptionAttributeResolver#ENABLE_JVM_STACKTRACE_PROPERTY}.
   */
  static ExceptionAttributeResolver getDefault() {
    return getDefault(
        Boolean.parseBoolean(ConfigUtil.getString(ENABLE_JVM_STACKTRACE_PROPERTY, "false")));
  }

  /**
   * Return the default exception attribute resolver.
   *
   * @param jvmStacktraceEnabled if true, resolve stacktrace using the stacktrace renderer built
   *     into the JVM. This built in JVM renderer is not attribute limits aware, and may utilize
   *     more CPU / memory than is needed. Most users will prefer to set this to {@code false}.
   */
  static ExceptionAttributeResolver getDefault(boolean jvmStacktraceEnabled) {
    return new DefaultExceptionAttributeResolver(jvmStacktraceEnabled);
  }

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
