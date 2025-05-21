package io.opentelemetry.sdk.entities;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract public class Entity {

  public static Entity create(String id, String name, Attributes attributes) {
    return new AutoValue_Entity(id, name, attributes);
  }

  public abstract String getId();

  public abstract String getName();

  public abstract Attributes getAttributes();

  public Entity withAttributes(Attributes newAttributes) {
    return new AutoValue_Entity(getId(), getName(), newAttributes);
  }

}
