package io.opentelemetry.sdk.entities;

import io.opentelemetry.sdk.resources.Resource;

public interface EntityListener {

  void onEntityState(Entity state, Resource resource);
  void onEntityDelete(Entity state, Resource resource);
}
