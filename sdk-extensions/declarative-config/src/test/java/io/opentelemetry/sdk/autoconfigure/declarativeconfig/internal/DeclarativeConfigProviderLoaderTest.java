/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.ClearSystemProperty;

@ClearSystemProperty(
    key =
        "io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal.DeclarativeConfigProviderLoaderTest$TestSpi")
class DeclarativeConfigProviderLoaderTest {

  private static final TestSpi FIRST = new FirstProvider();
  private static final TestSpi SECOND = new SecondProvider();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(DeclarativeConfigProviderLoader.class);

  private final ComponentLoader loader = mock(ComponentLoader.class);

  @Test
  void cachesSelectedProviders() {
    assertThat(DeclarativeConfigProviderLoader.getMapper())
        .isSameAs(DeclarativeConfigProviderLoader.getMapper());
    assertThat(DeclarativeConfigProviderLoader.getYamlParser())
        .isSameAs(DeclarativeConfigProviderLoader.getYamlParser());
  }

  @Test
  void retriesFailedResolutionAndCachesSuccess() {
    AtomicReference<TestSpi> ref = new AtomicReference<>();
    when(loader.load(TestSpi.class))
        .thenThrow(new IllegalStateException("lookup failed"))
        .thenReturn(Collections.singletonList(FIRST));

    assertThatThrownBy(() ->
            DeclarativeConfigProviderLoader.resolveProvider(ref, TestSpi.class, loader, "missing"))
        .isInstanceOf(IllegalStateException.class);
    assertThat(ref.get()).isNull();
    assertThat(
            DeclarativeConfigProviderLoader.resolveProvider(ref, TestSpi.class, loader, "missing"))
        .isSameAs(FIRST);
    assertThat(
            DeclarativeConfigProviderLoader.resolveProvider(ref, TestSpi.class, loader, "missing"))
        .isSameAs(FIRST);
    verify(loader, times(2)).load(TestSpi.class);
  }

  @Test
  void rejectsMissingProvider() {
    when(loader.load(TestSpi.class)).thenReturn(Collections.emptyList());

    assertThatThrownBy(() ->
            DeclarativeConfigProviderLoader.resolveProvider(
                new AtomicReference<>(), TestSpi.class, loader, "missing"))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("missing");
  }

  @Test
  void selectsSingleProvider() {
    when(loader.load(TestSpi.class)).thenReturn(Collections.singletonList(FIRST));

    assertThat(
            DeclarativeConfigProviderLoader.resolveProvider(
                new AtomicReference<>(), TestSpi.class, loader, "missing"))
        .isSameAs(FIRST);
  }

  @Test
  void selectsFirstProviderAndWarns() {
    when(loader.load(TestSpi.class)).thenReturn(Arrays.asList(FIRST, SECOND));

    assertThat(
            DeclarativeConfigProviderLoader.resolveProvider(
                new AtomicReference<>(), TestSpi.class, loader, "missing"))
        .isSameAs(FIRST);
    logs.assertContains(
        event -> event.getMessage().startsWith("Multiple {0} providers found."),
        "Expected multiple-provider warning");
  }

  @Test
  void selectsConfiguredProvider() {
    System.setProperty(TestSpi.class.getName(), SECOND.getClass().getName());
    when(loader.load(TestSpi.class)).thenReturn(Arrays.asList(FIRST, SECOND));

    assertThat(
            DeclarativeConfigProviderLoader.resolveProvider(
                new AtomicReference<>(), TestSpi.class, loader, "missing"))
        .isSameAs(SECOND);
  }

  @Test
  void rejectsUnknownConfiguredProvider() {
    System.setProperty(TestSpi.class.getName(), "missing.Provider");
    when(loader.load(TestSpi.class)).thenReturn(Arrays.asList(FIRST, SECOND));

    assertThatThrownBy(() ->
            DeclarativeConfigProviderLoader.resolveProvider(
                new AtomicReference<>(), TestSpi.class, loader, "missing"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("missing.Provider");
  }

  private interface TestSpi {}

  private static final class FirstProvider implements TestSpi {}

  private static final class SecondProvider implements TestSpi {}
}
