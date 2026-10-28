/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class IncubatorDeclarativeConfigPropertiesAdapterTest {

  @Test
  void testAdapterDelegation() {
    DeclarativeConfigProperties delegate = mock(DeclarativeConfigProperties.class);
    DeclarativeConfigProperties child = mock(DeclarativeConfigProperties.class);
    ComponentLoader componentLoader = mock(ComponentLoader.class);

    when(delegate.getString("str")).thenReturn("hello");
    when(delegate.getBoolean("bool")).thenReturn(true);
    when(delegate.getInt("int")).thenReturn(42);
    when(delegate.getLong("long")).thenReturn(100L);
    when(delegate.getDouble("double")).thenReturn(3.14);

    when(delegate.isString("str")).thenReturn(true);
    when(delegate.isBoolean("bool")).thenReturn(true);
    when(delegate.isInt("int")).thenReturn(true);
    when(delegate.isLong("long")).thenReturn(true);
    when(delegate.isDouble("double")).thenReturn(true);

    when(delegate.getScalarList("list", String.class))
        .thenReturn(Collections.singletonList("item"));
    when(delegate.getStructured("child")).thenReturn(child);
    when(delegate.getStructured("missing")).thenReturn(null);

    when(delegate.getStructuredList("children")).thenReturn(Collections.singletonList(child));
    when(delegate.getStructuredList("missing_list")).thenReturn(null);

    when(delegate.getPropertyKeys()).thenReturn(Collections.singleton("str"));
    when(delegate.getComponentLoader()).thenReturn(componentLoader);
    when(delegate.toString()).thenReturn("mockDelegate");

    IncubatorDeclarativeConfigPropertiesAdapter adapter =
        new IncubatorDeclarativeConfigPropertiesAdapter(delegate);

    assertThat(adapter.getString("str")).isEqualTo("hello");
    assertThat(adapter.getBoolean("bool")).isTrue();
    assertThat(adapter.getInt("int")).isEqualTo(42);
    assertThat(adapter.getLong("long")).isEqualTo(100L);
    assertThat(adapter.getDouble("double")).isEqualTo(3.14);

    assertThat(adapter.isString("str")).isTrue();
    assertThat(adapter.isBoolean("bool")).isTrue();
    assertThat(adapter.isInt("int")).isTrue();
    assertThat(adapter.isLong("long")).isTrue();
    assertThat(adapter.isDouble("double")).isTrue();

    assertThat(adapter.getScalarList("list", String.class)).containsExactly("item");

    DeclarativeConfigProperties adaptedChild = adapter.getStructured("child");
    assertThat(adaptedChild).isInstanceOf(IncubatorDeclarativeConfigPropertiesAdapter.class);
    assertThat(adapter.getStructured("missing")).isNull();

    List<DeclarativeConfigProperties> adaptedChildren = adapter.getStructuredList("children");
    assertThat(adaptedChildren).hasSize(1);
    assertThat(adaptedChildren.get(0))
        .isInstanceOf(IncubatorDeclarativeConfigPropertiesAdapter.class);
    assertThat(adapter.getStructuredList("missing_list")).isNull();

    assertThat(adapter.getPropertyKeys()).containsExactly("str");
    assertThat(adapter.getComponentLoader()).isSameAs(componentLoader);
    assertThat(adapter.toString())
        .isEqualTo("IncubatorDeclarativeConfigPropertiesAdapter{mockDelegate}");
  }
}
