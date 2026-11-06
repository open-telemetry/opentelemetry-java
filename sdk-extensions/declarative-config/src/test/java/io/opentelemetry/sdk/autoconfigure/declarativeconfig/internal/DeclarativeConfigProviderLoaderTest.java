/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.common.ComponentLoader;
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
  void loadFirstOrConfiguredUsesDefaultWhenNoProvidersAreRegistered() {
    AtomicBoolean defaultProviderCalled = new AtomicBoolean();
    Supplier<SelectionTestSpi> defaultProvider =
        () -> {
          defaultProviderCalled.set(true);
          return new FirstSelectionTestProvider();
        };

    SelectionTestSpi selected =
        DeclarativeConfigProviderLoader.loadFirstOrConfigured(
            SelectionTestSpi.class,
            defaultProvider,
            SelectionTestSpi.class.getName(),
            componentLoader(),
            "");

    assertThat(defaultProviderCalled).isTrue();
    assertThat(selected).isInstanceOf(FirstSelectionTestProvider.class);
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

  private interface SelectionTestSpi {}

  private static final class FirstSelectionTestProvider implements SelectionTestSpi {}

  private static final class SecondSelectionTestProvider implements SelectionTestSpi {}
}
