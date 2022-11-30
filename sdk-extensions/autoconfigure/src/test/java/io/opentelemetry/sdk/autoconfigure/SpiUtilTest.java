/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SpiUtilTest {

  private static final ConfigProperties EMPTY =
      DefaultConfigProperties.createForTest(Collections.emptyMap());

  @Test
  public void canRetrieveByName() {
    SpiUtil.ServiceLoaderFinder mockFinder = mock(SpiUtil.ServiceLoaderFinder.class);
    when(mockFinder.load(any(), any()))
        .thenReturn(Collections.singletonList(new SpiExampleProviderImplementation()));

    NamedSpiManager<SpiExample> spiProvider =
        SpiUtil.loadConfigurable(
            SpiExampleProvider.class,
            SpiExampleProvider::getName,
            SpiExampleProvider::createSpiExample,
            EMPTY,
            SpiUtilTest.class.getClassLoader(),
            mockFinder);

    assertThat(spiProvider.getByName(SpiExampleProviderImplementation.NAME)).isNotNull();
    assertThat(spiProvider.getByName("invalid-provider")).isNull();
  }

  @Test
  public void instantiatesImplementationsLazily() {
    SpiExampleProvider mockProvider = mock(SpiExampleProvider.class);
    when(mockProvider.getName()).thenReturn("lazy-init-example");

    SpiUtil.ServiceLoaderFinder mockFinder = mock(SpiUtil.ServiceLoaderFinder.class);
    when(mockFinder.load(any(), any())).thenReturn(Collections.singletonList(mockProvider));

    NamedSpiManager<SpiExample> spiProvider =
        SpiUtil.loadConfigurable(
            SpiExampleProvider.class,
            SpiExampleProvider::getName,
            SpiExampleProvider::createSpiExample,
            EMPTY,
            SpiUtilTest.class.getClassLoader(),
            mockFinder);

    verify(mockProvider, never()).createSpiExample(any()); // not requested yet
    spiProvider.getByName("lazy-init-example");
    verify(mockProvider).createSpiExample(EMPTY); // initiated upon request
  }

  @Test
  public void onlyInstantiatesOnce() {
    SpiUtil.ServiceLoaderFinder mockFinder = mock(SpiUtil.ServiceLoaderFinder.class);
    when(mockFinder.load(any(), any()))
        .thenReturn(Collections.singletonList(new SpiExampleProviderImplementation()));

    NamedSpiManager<SpiExample> spiProvider =
        SpiUtil.loadConfigurable(
            SpiExampleProvider.class,
            SpiExampleProvider::getName,
            SpiExampleProvider::createSpiExample,
            EMPTY,
            SpiUtilTest.class.getClassLoader(),
            mockFinder);

    SpiExample first = spiProvider.getByName(SpiExampleProviderImplementation.NAME);
    SpiExample second = spiProvider.getByName(SpiExampleProviderImplementation.NAME);
    assertThat(second).isEqualTo(first);
  }

  @Test
  public void failureToInitializeThrows() {
    String exceptionMessage = "failure to initialize should throw";
    SpiExampleProvider mockProvider = mock(SpiExampleProvider.class);
    when(mockProvider.getName()).thenReturn("init-failure-example");
    when(mockProvider.createSpiExample(any())).thenThrow(new RuntimeException());

    SpiUtil.ServiceLoaderFinder mockFinder = mock(SpiUtil.ServiceLoaderFinder.class);
    when(mockFinder.load(any(), any())).thenReturn(Collections.singletonList(mockProvider));

    NamedSpiManager<SpiExample> spiProvider =
        SpiUtil.loadConfigurable(
            SpiExampleProvider.class,
            SpiExampleProvider::getName,
            SpiExampleProvider::createSpiExample,
            EMPTY,
            SpiUtilTest.class.getClassLoader(),
            mockFinder);

    assertThatThrownBy(() -> spiProvider.getByName("init-failure-example"))
        .withFailMessage(exceptionMessage)
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void loadsOrderedSpi() {
    ResourceProvider spi1 = mock(ResourceProvider.class);
    ResourceProvider spi2 = mock(ResourceProvider.class);
    ResourceProvider spi3 = mock(ResourceProvider.class);

    when(spi1.order()).thenReturn(2);
    when(spi2.order()).thenReturn(0);
    when(spi3.order()).thenReturn(1);

    SpiUtil.ServiceLoaderFinder mockFinder = mock(SpiUtil.ServiceLoaderFinder.class);
    when(mockFinder.load(ResourceProvider.class, SpiUtil.class.getClassLoader()))
        .thenReturn(asList(spi1, spi2, spi3));

    List<ResourceProvider> loadedSpi =
        SpiUtil.loadOrdered(ResourceProvider.class, SpiUtil.class.getClassLoader(), mockFinder);

    assertThat(loadedSpi).containsExactly(spi2, spi3, spi1);
  }

  private interface SpiExampleProvider {

    String getName();

    SpiExample createSpiExample(ConfigProperties config);
  }

  private static class SpiExampleProviderImplementation implements SpiExampleProvider {

    private static final String NAME = "spi-example";

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    public SpiExample createSpiExample(ConfigProperties config) {
      return new SpiExample() {};
    }
  }

  private interface SpiExample {}
}
