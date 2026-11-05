/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DeclarativeConfigProviderLoaderTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(DeclarativeConfigProviderLoader.class);

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

  @Test
  void loadFirstOrConfiguredUsesSingleProvider() {
    FirstSelectionTestProvider provider = new FirstSelectionTestProvider();

    SelectionTestSpi selected =
        DeclarativeConfigProviderLoader.loadFirstOrConfigured(
            SelectionTestSpi.class,
            FirstSelectionTestProvider::new,
            SelectionTestSpi.class.getName(),
            componentLoader(provider),
            "");

    assertThat(selected).isSameAs(provider);
  }

  @Test
  void loadFirstOrConfiguredUsesFirstProviderAndWarnsWhenMultipleAreUnconfigured() {
    FirstSelectionTestProvider firstProvider = new FirstSelectionTestProvider();
    SecondSelectionTestProvider secondProvider = new SecondSelectionTestProvider();

    SelectionTestSpi selected =
        DeclarativeConfigProviderLoader.loadFirstOrConfigured(
            SelectionTestSpi.class,
            FirstSelectionTestProvider::new,
            SelectionTestSpi.class.getName(),
            componentLoader(firstProvider, secondProvider),
            "");

    assertThat(selected).isSameAs(firstProvider);
    logs.assertContains("Multiple SelectionTestSpi providers found.");
  }

  @Test
  void loadFirstOrConfiguredUsesConfiguredProviderWhenMultipleArePresent() {
    FirstSelectionTestProvider firstProvider = new FirstSelectionTestProvider();
    SecondSelectionTestProvider selectedProvider = new SecondSelectionTestProvider();

    SelectionTestSpi selected =
        DeclarativeConfigProviderLoader.loadFirstOrConfigured(
            SelectionTestSpi.class,
            FirstSelectionTestProvider::new,
            SelectionTestSpi.class.getName(),
            componentLoader(firstProvider, selectedProvider),
            selectedProvider.getClass().getName());

    assertThat(selected).isSameAs(selectedProvider);
  }

  private static ComponentLoader componentLoader(Object... providers) {
    return new ComponentLoader() {
      @Override
      public <T> Iterable<T> load(Class<T> spiClass) {
        List<T> result = new ArrayList<>();
        for (Object provider : providers) {
          result.add(spiClass.cast(provider));
        }
        return result;
      }
    };
  }

  private interface TestSpi extends Ordered {}

  private interface SelectionTestSpi {}

  private static final class FirstSelectionTestProvider implements SelectionTestSpi {}

  private static final class SecondSelectionTestProvider implements SelectionTestSpi {}

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
