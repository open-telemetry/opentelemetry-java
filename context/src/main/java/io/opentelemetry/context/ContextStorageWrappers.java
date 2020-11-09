/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holder of functions to wrap the used {@link ContextStorage}. Separate class from {@link
 * LazyStorage} to allow registering wrappers before initializing storage.
 */
final class ContextStorageWrappers {

  private static final Logger log = Logger.getLogger(ContextStorageWrappers.class.getName());

  private static boolean storageInitialized;

  private static final List<Function<? super ContextStorage, ? extends ContextStorage>> wrappers =
      new ArrayList<>();

  static synchronized void addWrapper(
      Function<? super ContextStorage, ? extends ContextStorage> wrapper) {
    if (storageInitialized) {
      log.log(
          Level.FINE,
          "ContextStorage has already been initialized, ignoring call to add wrapper.",
          new Throwable());
      return;
    }
    wrappers.add(wrapper);
  }

  static synchronized List<Function<? super ContextStorage, ? extends ContextStorage>>
      getWrappers() {
    return wrappers;
  }

  static synchronized void setStorageInitialized() {
    storageInitialized = true;
  }

  private ContextStorageWrappers() {}
}
