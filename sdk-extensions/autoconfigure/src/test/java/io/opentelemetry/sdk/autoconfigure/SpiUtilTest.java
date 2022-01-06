/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Collections;
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
            Collections.singletonList(SpiExampleProviderImplementation.NAME),
            SpiExampleProvider::getName,
            SpiExampleProvider::createSpiExample,
            EMPTY,
            SpiUtilTest.class.getClassLoader(),
            mockFinder);

    assertNotNull(spiProvider.getByName(SpiExampleProviderImplementation.NAME));
    assertNull(spiProvider.getByName("invalid-provider"));
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
            Collections.singletonList(SpiExampleProviderImplementation.NAME),
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
            Collections.singletonList(SpiExampleProviderImplementation.NAME),
            SpiExampleProvider::getName,
            SpiExampleProvider::createSpiExample,
            EMPTY,
            SpiUtilTest.class.getClassLoader(),
            mockFinder);

    SpiExample first = spiProvider.getByName(SpiExampleProviderImplementation.NAME);
    SpiExample second = spiProvider.getByName(SpiExampleProviderImplementation.NAME);
    assertEquals(first, second);
  }

  @Test
  public void failureToInitializeCaughtReturnNull() {
    SpiExampleProvider mockProvider = mock(SpiExampleProvider.class);
    when(mockProvider.getName()).thenReturn("init-failure-example");
    when(mockProvider.createSpiExample(any()))
        .thenThrow(new RuntimeException("failure to initialize should catch/ignore"));

    SpiUtil.ServiceLoaderFinder mockFinder = mock(SpiUtil.ServiceLoaderFinder.class);
    when(mockFinder.load(any(), any())).thenReturn(Collections.singletonList(mockProvider));

    NamedSpiManager<SpiExample> spiProvider =
        SpiUtil.loadConfigurable(
            SpiExampleProvider.class,
            Collections.singletonList(SpiExampleProviderImplementation.NAME),
            SpiExampleProvider::getName,
            SpiExampleProvider::createSpiExample,
            EMPTY,
            SpiUtilTest.class.getClassLoader(),
            mockFinder);

    assertNull(spiProvider.getByName("init-failure-example"));
    verify(mockProvider).createSpiExample(EMPTY); // tried to initialize but failed
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
