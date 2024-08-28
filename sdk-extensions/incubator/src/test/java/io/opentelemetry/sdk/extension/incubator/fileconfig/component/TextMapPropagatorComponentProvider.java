/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

public class TextMapPropagatorComponentProvider implements ComponentProvider<TextMapPropagator> {
  @Override
  public Class<TextMapPropagator> getType() {
    return TextMapPropagator.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public TextMapPropagator create(StructuredConfigProperties config) {
    return new TestTextMapPropagator(config);
  }

  public static class TestTextMapPropagator implements TextMapPropagator {

    public final StructuredConfigProperties config;

    private TestTextMapPropagator(StructuredConfigProperties config) {
      this.config = config;
    }

    @Override
    public Collection<String> fields() {
      return Collections.emptyList();
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {}

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
      return context;
    }
  }
}
