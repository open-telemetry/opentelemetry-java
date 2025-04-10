/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.customchecks;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class OtelInternalJavadocTest {

  @Test
  void positiveCases() {
    CompilationTestHelper.newInstance(OtelInternalJavadoc.class, OtelInternalJavadocTest.class)
        .addSourceLines(
            "internal/InternalJavadocPositiveCases.java",
            "/*",
            " * Copyright The OpenTelemetry Authors",
            " * SPDX-License-Identifier: Apache-2.0",
            " */",
            "package io.opentelemetry.gradle.customchecks.internal;",
            "// BUG: Diagnostic contains: doesn't end with any of the applicable javadoc disclaimers",
            "public class InternalJavadocPositiveCases {",
            "  // BUG: Diagnostic contains: doesn't end with any of the applicable javadoc disclaimers",
            "  public static class One {}",
            "  /** Doesn't have the disclaimer. */",
            "  // BUG: Diagnostic contains: doesn't end with any of the applicable javadoc disclaimers",
            "  public static class Two {}",
            "}")
        .doTest();
  }

  @Test
  void negativeCases() {
    CompilationTestHelper.newInstance(OtelInternalJavadoc.class, OtelInternalJavadocTest.class)
        .addSourceLines(
            "internal/InternalJavadocNegativeCases.java",
            "/*",
            " * Copyright The OpenTelemetry Authors",
            " * SPDX-License-Identifier: Apache-2.0",
            " */",
            "package io.opentelemetry.gradle.customchecks.internal;",
            "/**",
            " * This class is internal and is hence not for public use. Its APIs are unstable and can change at",
            " * any time.",
            " */",
            "public class InternalJavadocNegativeCases {",
            "  /**",
            "   * This class is internal and is hence not for public use. Its APIs are unstable and can change at",
            "   * any time.",
            "   */",
            "  public static class One {}",
            "  static class Two {}",
            "}")
        .doTest();
  }
}
