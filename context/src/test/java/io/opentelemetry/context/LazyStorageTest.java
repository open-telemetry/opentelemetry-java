/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

class LazyStorageTest {

  private static final String CONTEXT_STORAGE_PROVIDER_PROPERTY =
      "io.opentelemetry.context.contextStorageProvider";
  private static final String MOCK_CONTEXT_STORAGE_PROVIDER =
      "io.opentelemetry.context.LazyStorageTest$MockContextStorageProvider";
  private static final AtomicReference<Throwable> DEFERRED_STORAGE_FAILURE =
      new AtomicReference<>();

  @Test
  @ClearSystemProperty(key = CONTEXT_STORAGE_PROVIDER_PROPERTY)
  void empty_providers() {
    assertThat(LazyStorage.createStorage(DEFERRED_STORAGE_FAILURE))
        .isEqualTo(DefaultContext.threadLocalStorage());
  }

  @Test
  @SetSystemProperty(key = CONTEXT_STORAGE_PROVIDER_PROPERTY, value = MOCK_CONTEXT_STORAGE_PROVIDER)
  void set_storage_provider_property_and_empty_providers() {
    assertThat(LazyStorage.createStorage(DEFERRED_STORAGE_FAILURE))
        .isEqualTo(DefaultContext.threadLocalStorage());
  }

  @Test
  @ClearSystemProperty(key = CONTEXT_STORAGE_PROVIDER_PROPERTY)
  void unset_storage_provider_property_and_one_providers() throws Exception {
    File serviceFile = createContextStorageProvider();
    try {
      assertThat(LazyStorage.createStorage(DEFERRED_STORAGE_FAILURE)).isEqualTo(mockContextStorage);
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  @SetSystemProperty(
      key = CONTEXT_STORAGE_PROVIDER_PROPERTY,
      value = "not.match.provider.class.name")
  void set_storage_provider_property_not_matches_one_providers() throws Exception {
    File serviceFile = createContextStorageProvider();
    try {
      assertThat(LazyStorage.createStorage(DEFERRED_STORAGE_FAILURE))
          .isEqualTo(DefaultContext.threadLocalStorage());
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  @SetSystemProperty(key = CONTEXT_STORAGE_PROVIDER_PROPERTY, value = MOCK_CONTEXT_STORAGE_PROVIDER)
  void set_storage_provider_property_matches_one_providers() throws Exception {
    File serviceFile = createContextStorageProvider();
    try {
      assertThat(LazyStorage.createStorage(DEFERRED_STORAGE_FAILURE)).isEqualTo(mockContextStorage);
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  @SetSystemProperty(key = CONTEXT_STORAGE_PROVIDER_PROPERTY, value = "default")
  void enforce_default_and_empty_providers() {
    assertThat(LazyStorage.createStorage(DEFERRED_STORAGE_FAILURE))
        .isEqualTo(DefaultContext.threadLocalStorage());
  }

  @Test
  @SetSystemProperty(key = CONTEXT_STORAGE_PROVIDER_PROPERTY, value = "default")
  void enforce_default_and_one_providers() throws IOException {
    File serviceFile = createContextStorageProvider();
    try {
      assertThat(LazyStorage.createStorage(DEFERRED_STORAGE_FAILURE))
          .isEqualTo(DefaultContext.threadLocalStorage());
    } finally {
      serviceFile.delete();
    }
  }

  private static File createContextStorageProvider() throws IOException {
    URL location =
        MockContextStorageProvider.class.getProtectionDomain().getCodeSource().getLocation();
    File file =
        new File(
            location.getPath() + "META-INF/services/" + ContextStorageProvider.class.getName());
    file.getParentFile().mkdirs();

    @SuppressWarnings("DefaultCharset")
    Writer output = new FileWriter(file);
    output.write(MockContextStorageProvider.class.getName());
    output.close();

    return file;
  }

  private static final ContextStorage mockContextStorage =
      new ContextStorage() {
        @Override
        public Scope attach(Context toAttach) {
          return null;
        }

        @Override
        public Context current() {
          return null;
        }
      };

  public static final class MockContextStorageProvider implements ContextStorageProvider {
    @Override
    public ContextStorage get() {
      return mockContextStorage;
    }
  }
}
