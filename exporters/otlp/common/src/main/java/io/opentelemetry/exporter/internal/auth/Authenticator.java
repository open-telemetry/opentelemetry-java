/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.auth;

import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

/** Internal class to allow users of OTLP-OkHttp exporters to add support for authentication. */
public interface Authenticator {

  /**
   * Method called by the exporter to get headers to be used on a request that requires
   * authentication.
   *
   * @param headers Consumer callback to be used to propagate headers to the underlying OTLP HTTP
   *     exporter implementation.
   */
  void getHeaders(Consumer<Map<String, String>> headers);

  /**
   * Reflectively access a {@link GrpcExporterBuilder}, or {@link OkHttpExporterBuilder} instance in
   * field called "delegate" of the instance, and set the {@link RetryPolicy}.
   *
   * @throws IllegalArgumentException if the instance does not contain a field called "delegate" of
   *     a supported type.
   */
  public static void setAuthenticatorOnDelegate(Object builder, Authenticator authenticator) {
    try {
      Field field = builder.getClass().getDeclaredField("delegate");
      field.setAccessible(true);
      Object value = field.get(builder);
      if (value instanceof GrpcExporterBuilder) {
        throw new IllegalArgumentException("DefaultGrpcExporterBuilder not supported yet.");
      } else if (value instanceof OkHttpExporterBuilder) {
        ((OkHttpExporterBuilder<?>) value).setAuthenticator(authenticator);
      } else {
        throw new IllegalArgumentException(
            "Delegate field is not type DefaultGrpcExporterBuilder or OkHttpGrpcExporterBuilder.");
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException("Unable to access delegate reflectively.", e);
    }
  }
}
