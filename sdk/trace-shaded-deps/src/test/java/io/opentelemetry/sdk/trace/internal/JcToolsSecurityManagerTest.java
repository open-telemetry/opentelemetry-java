/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

public class JcToolsSecurityManagerTest {

  @Test
  @EnabledOnJre({JRE.JAVA_8, JRE.JAVA_11, JRE.JAVA_17})
  @SuppressLogger(JcTools.class)
  void newFixedSizeQueue_SunMiscProhibited() {
    assertThat(System.getSecurityManager()).isNull();
    SunMiscProhibitedSecurityManager testingSecurityManager =
        new SunMiscProhibitedSecurityManager();
    try {
      System.setSecurityManager(testingSecurityManager);
      Queue<Object> queue =
          AccessController.doPrivileged(
              (PrivilegedAction<Queue<Object>>) () -> JcTools.newFixedSizeQueue(10));
      assertThat(queue).isInstanceOf(ArrayBlockingQueue.class);
    } finally {
      System.setSecurityManager(null);
    }
  }
}
