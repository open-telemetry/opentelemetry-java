/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.internal.testing;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** An extension that assists in cleaning up {@link Closeable}s after test completion. */
public class CleanupExtension implements AfterEachCallback {

  private final List<Closeable> closeables = new ArrayList<>();

  /** Add {@link Closeable}s to be cleaned up after test completion. */
  public void addCloseables(Collection<Closeable> closeables) {
    this.closeables.addAll(closeables);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    for (Closeable closeable : closeables) {
      try {
        closeable.close();
      } catch (IOException e) {
        // Ignore
      }
    }
    closeables.clear();
  }
}
