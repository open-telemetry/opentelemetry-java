/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.AccessControlException;
import java.security.Permission;

/**
 * A security manager which disallows access to classes in sun.misc. Running the tests with a
 * standard security manager is too invasive.
 */
public class SunMiscProhibitedSecurityManager extends SecurityManager {

  public SunMiscProhibitedSecurityManager() {}

  @Override
  protected Class<?>[] getClassContext() {
    return super.getClassContext();
  }

  @Override
  public void checkPermission(Permission perm) {
    if (perm.getName().equals("accessClassInPackage.sun.misc")) {
      throw new AccessControlException("access denied " + perm, perm);
    }
  }

  @Override
  public void checkPermission(Permission perm, Object context) {}

  @Override
  public void checkCreateClassLoader() {}

  @Override
  public void checkAccess(Thread t) {}

  @Override
  public void checkAccess(ThreadGroup g) {}

  @Override
  public void checkExit(int status) {}

  @Override
  public void checkExec(String cmd) {}

  @Override
  public void checkLink(String lib) {}

  @Override
  public void checkRead(FileDescriptor fd) {}

  @Override
  public void checkRead(String file) {}

  @Override
  public void checkRead(String file, Object context) {}

  @Override
  public void checkWrite(FileDescriptor fd) {}

  @Override
  public void checkWrite(String file) {}

  @Override
  public void checkDelete(String file) {}

  @Override
  public void checkConnect(String host, int port) {}

  @Override
  public void checkConnect(String host, int port, Object context) {}

  @Override
  public void checkListen(int port) {}

  @Override
  public void checkAccept(String host, int port) {}

  @Override
  public void checkMulticast(InetAddress maddr) {}

  @Override
  public void checkPropertiesAccess() {}

  @Override
  public void checkPropertyAccess(String key) {}

  @Override
  public void checkPrintJobAccess() {}

  @Override
  public void checkPackageAccess(String pkg) {
    if (pkg.equals("sun.misc")) {
      super.checkPackageAccess(pkg);
    }
  }

  @Override
  public void checkPackageDefinition(String pkg) {}

  @Override
  public void checkSetFactory() {}

  @Override
  public void checkSecurityAccess(String target) {}
}
