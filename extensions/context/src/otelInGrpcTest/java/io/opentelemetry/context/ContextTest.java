package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ContextTest {

  private static final ContextKey<String> ANIMAL = Context.key("animal");

  private static final io.grpc.Context.Key<String> FOOD = io.grpc.Context.key("food");

  @AfterEach
  void cleanedContext() {
    assertThat(Context.current()).isSameAs(Context.root());
  }

  @Test
  void defaultIsRoot() {
    assertThat(Context.current()).isSameAs(Context.root());
  }

  @Test
  void attachNewContext() {
    Context context1 = Context.current().withValue(ANIMAL, "dog");
    Context context2 = Context.current().withValue(ANIMAL, "cat");

    assertThat(context1).isNotSameAs(Context.root());
    assertThat(Context.current()).isSameAs(Context.root());

    try (Scope ignored = context1.attach()) {
      assertThat(Context.current()).isSameAs(context1);

      try (Scope ignored2 = context2.attach()) {
        assertThat(Context.current()).isSameAs(context2);
      }

      assertThat(Context.current()).isSameAs(context1);
    }

    assertThat(Context.current()).isSameAs(Context.root());
  }

  @Test
  void otelAndGrpc() {
    Context otelContext1 = Context.current().withValue(ANIMAL, "dog");
    io.grpc.Context grpcContext1 = io.grpc.Context.current().withValue(FOOD, "banana");

    assertThat(Context.current().getValue(ANIMAL)).isNull();
    assertThat(FOOD.get()).isNull();

    io.grpc.Context toRestore;

    try (Scope ignored = otelContext1.attach()) {
      assertThat(Context.current().getValue(ANIMAL)).isEqualTo("dog");
      assertThat(FOOD.get()).isNull();

      toRestore = grpcContext1.attach();
      try {
        assertThat(Context.current().getValue(ANIMAL)).isEqualTo("dog");
        assertThat(FOOD.get()).isEqualTo("banana");
      } finally {
        grpcContext1.detach(toRestore);
      }
    }
  }
}
