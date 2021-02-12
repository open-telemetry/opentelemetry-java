package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.sdk.autoconfigure.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

/** {@link ResourceProvider} for automatically configuring {@link ProcessResource}. */
public class ProcessResourceProvider implements ResourceProvider {
  @Override
  public Resource createResource(
      ConfigProperties config) {
    return ProcessResource.getInstance();
  }
}
