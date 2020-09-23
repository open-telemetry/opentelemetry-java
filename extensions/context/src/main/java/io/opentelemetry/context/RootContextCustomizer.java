package io.opentelemetry.context;

/**
 * A Java SPI (Service Provider Interface) to allow modifying the root {@link Context}.
 */
public interface RootContextCustomizer {
  Context customize(Context context);
}
