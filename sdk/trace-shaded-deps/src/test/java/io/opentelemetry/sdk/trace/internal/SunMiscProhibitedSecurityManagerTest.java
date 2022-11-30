/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.AccessControlException;
import org.junit.jupiter.api.Test;

class SunMiscProhibitedSecurityManagerTest {

  @Test
  public void checkPackageAccess_ProhibitsSunMisc() {
    SunMiscProhibitedSecurityManager sm = new SunMiscProhibitedSecurityManager();
    assertThatThrownBy(() -> sm.checkPackageAccess("sun.misc"))
        .isInstanceOf(AccessControlException.class)
        .hasMessage(
            "access denied (\"java.lang.RuntimePermission\" \"accessClassInPackage.sun.misc\")");
  }

  @Test
  public void checkPackageAccess_ProhibitsSunMiscRuntimePermission() {
    SunMiscProhibitedSecurityManager sm = new SunMiscProhibitedSecurityManager();

    assertThatThrownBy(
            () -> sm.checkPermission(new RuntimePermission("accessClassInPackage.sun.misc")))
        .isInstanceOf(AccessControlException.class)
        .hasMessage(
            "access denied (\"java.lang.RuntimePermission\" \"accessClassInPackage.sun.misc\")");
  }

  @Test
  public void checkPackageAccess_AllowsOtherPackage() {
    SunMiscProhibitedSecurityManager sm = new SunMiscProhibitedSecurityManager();
    assertThatNoException().isThrownBy(() -> sm.checkPackageAccess("io.opentelemetry.sdk"));
  }
}
