package io.opentelemetry.exporter.internal.auth;

import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

public interface Authenticator {

    void getHeaders(Consumer<Map<String, String>> headers);

    public static void setAuthenticatorOnDelegate(Object builder, Authenticator authenticator) {
    try {
      Field field = builder.getClass().getDeclaredField("delegate");
      field.setAccessible(true);
      Object value = field.get(builder);
      if (value instanceof GrpcExporterBuilder) {
        throw new IllegalArgumentException(
            "DefaultGrpcExporterBuilder not supported yet.");
//        ((GrpcExporterBuilder<?>) value).setRetryPolicy(retryPolicy);
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
