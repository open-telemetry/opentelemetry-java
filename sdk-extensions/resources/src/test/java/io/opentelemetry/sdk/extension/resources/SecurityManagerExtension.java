/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import java.security.Permission;
import java.util.PropertyPermission;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

final class SecurityManagerExtension implements BeforeEachCallback, AfterEachCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(SecurityManagerExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    context.getStore(NAMESPACE).put(SecurityManager.class, System.getSecurityManager());
    System.setSecurityManager(BlockPropertiesAccess.INSTANCE);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    System.setSecurityManager(
        (SecurityManager) context.getStore(NAMESPACE).get(SecurityManager.class));
  }

  private static class BlockPropertiesAccess extends SecurityManager {

    private static final BlockPropertiesAccess INSTANCE = new BlockPropertiesAccess();

    @Override
    public void checkPermission(Permission perm) {
      if (perm instanceof PropertyPermission) {
        throw new SecurityException("Property access not allowed.");
      }
    }
  }
}
