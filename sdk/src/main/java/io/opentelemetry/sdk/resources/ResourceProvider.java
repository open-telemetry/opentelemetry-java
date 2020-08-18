package io.opentelemetry.sdk.resources;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableKeyValuePairs.KeyValueConsumer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * ResourceProvider is a service provider for additional {@link Resource}s.
 * Users of OpenTelemetry SDK can use it to add custom {@link Resource} attributes.
 *
 * <p>Fully qualified class name of the implementation should be registered in {@code
 * META-INF/services/io.opentelemetry.sdk.resources.ResourceProvider}.
 *
 * <p>Resources specified via system properties or environment variables will take precedence
 * over any value supplied via {@code ResourceProvider}.
 *
 * @see EnvAutodetectResource
 */
@ThreadSafe
public abstract class ResourceProvider {

  public Resource create() {
    final Attributes.Builder attrBuilder = Attributes.newBuilder();
    getAttributes().forEach(
        new KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            attrBuilder.setAttribute(key, value);
          }
        });

    return Resource.create(attrBuilder.build());
  }

  protected abstract Attributes getAttributes();
}
