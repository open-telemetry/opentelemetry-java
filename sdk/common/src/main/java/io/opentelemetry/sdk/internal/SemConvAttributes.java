package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Provides access to semantic convention attributes used within the SDK implementation.
 * This avoid having to pull in semantic conventions as a dependency, which would easily collide and conflict with user-provided dependencies.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class SemConvAttributes {

  private SemConvAttributes() {}

  // TODO: add tests against semconv
  public static final AttributeKey<String> OTEL_COMPONENT_TYPE =
      AttributeKey.stringKey("otel.component.type");
  public static final AttributeKey<String> OTEL_COMPONENT_NAME =
          AttributeKey.stringKey("otel.component.name");
  // TODO: add semconv test
  public static final AttributeKey<String> ERROR_TYPE =
      AttributeKey.stringKey("error.type");
}
