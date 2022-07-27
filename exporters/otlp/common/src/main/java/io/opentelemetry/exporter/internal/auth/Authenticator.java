/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.auth;

import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 *
 * <p>Allow users of OTLP-OkHttp exporters to add support for authentication.
 */
public interface Authenticator {

  /**
   * Method called by the exporter to get headers to be used on a request that requires
   * authentication.
   *
   * @return Headers to add to the request
   */
  Map<String, String> getHeaders();

  /**
   * Reflectively access a {@link GrpcExporterBuilder}, or {@link OkHttpExporterBuilder} instance in
   * field called "delegate" of the instance, and set the {@link Authenticator}.
   *
   * @param builder export builder to modify
   * @param authenticator authenticator to set on builder
   * @throws IllegalArgumentException if the instance does not contain a field called "delegate" of
   *     a supported type.
   */
  static void setAuthenticatorOnDelegate(Object builder, Authenticator authenticator) {
    try {
      Field field = builder.getClass().getDeclaredField("delegate");
      field.setAccessible(true);
      Object value = field.get(builder);
      if (value instanceof GrpcExporterBuilder) {
        throw new IllegalArgumentException("GrpcExporterBuilder not supported yet.");
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
