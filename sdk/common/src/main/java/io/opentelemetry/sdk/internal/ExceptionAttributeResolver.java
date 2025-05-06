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

  /**
   * Resolve the {@link #EXCEPTION_TYPE} attribute from the {@code throwable}, or {@code null} if no
   * value should be set.
   *
   * @param throwable the throwable
   * @param maxAttributeLength the max attribute length that will be retained by the SDK. Responses
   *     are not required to conform to this limit, but implementations may incorporate this limit
   *     to avoid unnecessary compute.
   */
  @Nullable
  String getExceptionType(Throwable throwable, int maxAttributeLength);

  /**
   * Resolve the {@link #EXCEPTION_MESSAGE} attribute from the {@code throwable}, or {@code null} if
   * no value should be set.
   *
   * @param throwable the throwable
   * @param maxAttributeLength the max attribute length that will be retained by the SDK. Responses
   *     are not required to conform to this limit, but implementations may incorporate this limit
   *     to avoid unnecessary compute.
   */
  @Nullable
  String getExceptionMessage(Throwable throwable, int maxAttributeLength);

  /**
   * Resolve the {@link #EXCEPTION_STACKTRACE} attribute from the {@code throwable}, or {@code null}
   * if no value should be set.
   *
   * @param throwable the throwable
   * @param maxAttributeLength the max attribute length that will be retained by the SDK. Responses
   *     are not required to conform to this limit, but implementations may incorporate this limit
   *     to avoid unnecessary compute.
   */
  @Nullable
  String getExceptionStacktrace(Throwable throwable, int maxAttributeLength);
}
