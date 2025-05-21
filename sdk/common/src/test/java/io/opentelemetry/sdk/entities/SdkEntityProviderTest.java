package io.opentelemetry.sdk.entities;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SdkEntityProviderTest {

  AttributeKey<String> k1 = stringKey("a1.k");
  AttributeKey<String> k2 = stringKey("a2.k");
  Attributes a1 = Attributes.of(k1, "boop");
  Attributes a2 = Attributes.of(k2, "bleep");
  Entity e1 = Entity.create("foo", "fooname", a1);
  Entity e2 = Entity.create("bar", "barname", a2);

  @Test
  void emptyListGivesConstructorGivesResource() {
    Resource res = new SdkEntityProvider().getResource();
    assertThat(res).isSameAs(Resource.empty());
  }

  @Test
  void constructorGivesResource() {
    List<Entity> entities = Arrays.asList(e1, e2);
    Resource res = new SdkEntityProvider(entities).getResource();
    assertThat(res.getAttributes().asMap()).containsOnly(
        entry(k1, "boop"),
        entry(k2, "bleep")
    );
  }

  @Test
  void constructWithIdCollisions() {
    Entity e1 = Entity.create("x", "fooname", a1);
    Entity e2 = Entity.create("x", "barname", a2);
    List<Entity> entities = Arrays.asList(e1, e2);

    Resource res = new SdkEntityProvider(entities).getResource();
    assertThat(res.getAttributes().asMap()).containsOnly(
        entry(k2, "bleep"));
  }

  @Test
  void addNewEntity() {
    Attributes newAttr = Attributes.of(stringKey("new key"), "newval");
    EntityListener listener = mock(EntityListener.class);
    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);

    doNothing().when(listener).onEntityState(entityCaptor.capture(), resourceCaptor.capture());

    EntityProvider entityProvider = new SdkEntityProvider(singletonList(e1));
    entityProvider.addListener(listener);

    entityProvider.addEntity("new id", "new name", newAttr);

    assertThat(entityCaptor.getValue().getId()).isEqualTo("new id");
    assertThat(entityCaptor.getValue().getName()).isEqualTo("new name");
    assertThat(entityProvider.getResource().getAttributes().asMap()).containsOnly(
        entry(k1, "boop"),
        entry(stringKey("new key"), "newval")
    );
  }

  @Test
  void addWithExistingIdOverridesEntity() {
    Attributes newAttr = Attributes.of(stringKey("jibro"), "newval");

    EntityListener listener = mock(EntityListener.class);

    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
    doNothing().when(listener).onEntityState(entityCaptor.capture(), resourceCaptor.capture());

    EntityProvider entityProvider = new SdkEntityProvider(singletonList(e1));
    entityProvider.addListener(listener);

    entityProvider.addEntity(e1.getId(), "new name", newAttr);

    assertThat(entityCaptor.getValue().getId()).isEqualTo(e1.getId());
    assertThat(entityCaptor.getValue().getName()).isEqualTo("new name");
    assertThat(resourceCaptor.getValue()).isSameAs(entityProvider.getResource());
    assertThat(entityProvider.getResource().getAttributes().asMap()).containsOnly(
        entry(stringKey("jibro"), "newval")
    );
  }

  @Test
  void updateNotFoundNoListeners() {
    EntityListener listener = mock(EntityListener.class);

    EntityProvider entityProvider = new SdkEntityProvider(singletonList(e1));
    entityProvider.addListener(listener);

    Resource resource = entityProvider.getResource();
    entityProvider.updateEntity("notfound", Attributes.empty());
    verifyNoInteractions(listener);
    assertThat(entityProvider.getResource()).isSameAs(resource);
  }

  @Test
  void updateEntity() {
    AttributeKey<String> newKey = stringKey("jibro");
    Attributes newAttr = Attributes.of(newKey, "newval");
    EntityListener listener = mock(EntityListener.class);

    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
    doNothing().when(listener).onEntityState(entityCaptor.capture(), resourceCaptor.capture());

    EntityProvider entityProvider = new SdkEntityProvider(singletonList(e1));
    entityProvider.addListener(listener);

    entityProvider.updateEntity(e1.getId(), newAttr);

    assertThat(entityCaptor.getValue().getId()).isEqualTo(e1.getId());
    assertThat(entityCaptor.getValue().getName()).isEqualTo(e1.getName());
    assertThat(entityCaptor.getValue().getAttributes()).isEqualTo(newAttr);
    assertThat(entityProvider.getResource()).isSameAs(resourceCaptor.getValue());
    assertThat(resourceCaptor.getValue().getAttributes().asMap()).containsOnly(
        entry(newKey, "newval")
    );
  }

  @Test
  void deleteEntity() {
    EntityListener listener = mock(EntityListener.class);

    ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
    ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
    doNothing().when(listener).onEntityDelete(entityCaptor.capture(), resourceCaptor.capture());

    EntityProvider entityProvider = new SdkEntityProvider(Arrays.asList(e1, e2));
    entityProvider.addListener(listener);

    entityProvider.deleteEntity(e1.getId());

    assertThat(entityCaptor.getValue().getId()).isEqualTo(e1.getId());
    assertThat(entityCaptor.getValue().getName()).isEqualTo(e1.getName());
    assertThat(entityProvider.getResource()).isSameAs(resourceCaptor.getValue());
    assertThat(resourceCaptor.getValue().getAttributes().asMap()).containsOnly(
        entry(k2, "bleep")
    );
  }

  @Test
  void deleteEntityNotFound() {
    EntityListener listener = mock(EntityListener.class);

    EntityProvider entityProvider = new SdkEntityProvider(Arrays.asList(e1, e2));

    Resource resource = entityProvider.getResource();
    entityProvider.addListener(listener);
    entityProvider.deleteEntity("NO WAY");
    verifyNoInteractions(listener);
    assertThat(entityProvider.getResource()).isSameAs(resource);
  }

  @Test
  void deleteLastReturnsEmptyResource() {
    EntityProvider entityProvider = new SdkEntityProvider(Arrays.asList(e1, e2));
    assertThat(entityProvider.getResource().getAttributes().size()).isPositive();
    entityProvider.deleteEntity(e1.getId());
    entityProvider.deleteEntity(e2.getId());
    assertThat(entityProvider.getResource().getAttributes().size()).isZero();
    assertThat(entityProvider.getResource()).isSameAs(Resource.empty());
  }

}
