/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

public class ComponentProviderExtension implements BeforeEachCallback {

  private final SpiHelper spiHelper =
      spy(SpiHelper.create(SpanExporterFactoryTest.class.getClassLoader()));
  private final DeclarativeConfigContext context = new DeclarativeConfigContext(spiHelper);

  private List<ComponentProvider<?>> loadedComponentProviders = Collections.emptyList();

  @SuppressWarnings("unchecked")
  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    when(spiHelper.load(ComponentProvider.class))
        .thenAnswer(
            invocation -> {
              List<ComponentProvider<?>> result =
                  (List<ComponentProvider<?>>) invocation.callRealMethod();

              // only capture first invocation for exporter, not second for authenticator
              if (loadedComponentProviders.isEmpty()) {
                loadedComponentProviders =
                    result.stream().map(Mockito::spy).collect(Collectors.toList());
                return loadedComponentProviders;
              }
              return result;
            });
  }

  DeclarativeConfigContext getContext() {
    return context;
  }

  ComponentProvider<?> getComponentProvider(String name, Class<?> type) {
    return loadedComponentProviders.stream()
        .filter(
            componentProvider ->
                componentProvider.getName().equals(name)
                    && componentProvider.getType().equals(type))
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
