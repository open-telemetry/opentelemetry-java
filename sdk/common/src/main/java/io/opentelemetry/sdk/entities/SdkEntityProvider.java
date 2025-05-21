package io.opentelemetry.sdk.entities;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SdkEntityProvider implements EntityProvider {

  private final Object lock = new Object();
  private final AtomicReference<Resource> resource = new AtomicReference<>(Resource.empty());
  @GuardedBy("lock")
  private final LinkedHashMap<String,Entity> entities = new LinkedHashMap<>();
  @GuardedBy("lock")
  private final List<EntityListener> listeners = new ArrayList<>();

  public SdkEntityProvider() {
    this(Collections.emptyList());
  }

  public SdkEntityProvider(List<Entity> initialEntities) {
    for (Entity entity : initialEntities) {
      entities.put(entity.getId(), entity);
    }
    rebuildResource();
  }

  @Override
  public Resource getResource() {
    Resource result = resource.get();
    return result == null ? Resource.empty() : result;
  }

  @Override
  public void addListener(EntityListener listener) {
    synchronized(lock){
      listeners.add(listener);
    }
  }

  @Override
  public void addEntity(String id, String name, Attributes attributes) {
    Entity entity = Entity.create(id, name, attributes);
    List<EntityListener> listeners;
    Resource resource;
    synchronized(lock){
      entities.remove(id);
      entities.put(id, entity);

      resource = rebuildResource();
      listeners = new ArrayList<>(this.listeners);
    }
    for (EntityListener listener : listeners) {
      listener.onEntityState(entity, resource);
    }
    
  }

  @Override
  public void updateEntity(String id, Attributes attributes) {
    List<EntityListener> listeners;
    Resource resource;
    Entity updatedEntity;
    synchronized(lock){
      Entity entity = entities.get(id);
      if(entity == null){
        return;
      }
      updatedEntity = entity.withAttributes(attributes);
      entities.put(id, updatedEntity);
      resource = rebuildResource();
      listeners = new ArrayList<>(this.listeners);
    }
    for (EntityListener listener : listeners) {
      listener.onEntityState(updatedEntity, resource);
    }
  }

  @Override
  public void deleteEntity(String id) {
    Entity removedEntity;
    Resource resource;
    synchronized(lock){
      removedEntity = entities.remove(id);
      if(removedEntity == null){
        return;
      }
      resource = rebuildResource();
    }
    for (EntityListener listener : listeners) {
      listener.onEntityDelete(removedEntity, resource);
    }
  }

//  private void doMutationInsideLock(){
//
//  }

  private Resource rebuildResource() {
    Resource newResource = doRebuildResource();
    resource.set(newResource);
    return newResource;
  }

  private Resource doRebuildResource() {
    if (entities.isEmpty()) {
      return Resource.empty();
    }
    ResourceBuilder builder = Resource.builder();
    for (Entity entity : entities.values()) {
      builder.putAll(entity.getAttributes());
    }
    return builder.build();
  }
}
