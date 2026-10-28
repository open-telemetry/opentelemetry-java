/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class DeclarativeConfigProviderLoaderTest {

  @Test
  void loadSelectsHighestOrderedProvider() {
    TestProvider lowerOrder = new TestProvider(1);
    TestProvider higherOrder = new TestProvider(2);
    ComponentLoader componentLoader = componentLoader(lowerOrder, higherOrder);

    TestSpi selected =
        DeclarativeConfigProviderLoader.load(
            TestSpi.class, () -> new TestProvider(Integer.MIN_VALUE), componentLoader);

    assertThat(selected).isSameAs(higherOrder);
  }

  @Test
  void loadUsesDefaultWhenNoProvidersAreRegistered() {
    AtomicBoolean defaultProviderCalled = new AtomicBoolean();
    Supplier<TestSpi> defaultProvider =
        () -> {
          defaultProviderCalled.set(true);
          return new TestProvider(Integer.MIN_VALUE);
        };

    TestSpi selected =
        DeclarativeConfigProviderLoader.load(TestSpi.class, defaultProvider, componentLoader());

    assertThat(defaultProviderCalled).isTrue();
    assertThat(selected).isInstanceOf(TestProvider.class);
  }

  @Test
  void loadDoesNotCreateDefaultWhenProviderIsRegistered() {
    AtomicBoolean defaultProviderCalled = new AtomicBoolean();
    TestProvider provider = new TestProvider(0);

    TestSpi selected =
        DeclarativeConfigProviderLoader.load(
            TestSpi.class,
            () -> {
              defaultProviderCalled.set(true);
              return new TestProvider(Integer.MIN_VALUE);
            },
            componentLoader(provider));

    assertThat(selected).isSameAs(provider);
    assertThat(defaultProviderCalled).isFalse();
  }

  private static ComponentLoader componentLoader(TestProvider... providers) {
    return new ComponentLoader() {
      @Override
      public <T> Iterable<T> load(Class<T> spiClass) {
        List<T> result = new ArrayList<>();
        for (TestProvider provider : providers) {
          result.add(spiClass.cast(provider));
        }
        return result;
      }
    };
  }

  private interface TestSpi extends Ordered {}

  private static final class TestProvider implements TestSpi {

    private final int order;

    private TestProvider(int order) {
      this.order = order;
    }

    @Override
    public int order() {
      return order;
    }
  }
}
