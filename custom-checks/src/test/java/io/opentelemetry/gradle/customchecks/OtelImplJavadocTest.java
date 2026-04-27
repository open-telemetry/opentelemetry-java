/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.customchecks;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class OtelImplJavadocTest {

  @Test
  void positiveCases() {
    CompilationTestHelper.newInstance(OtelImplJavadoc.class, OtelImplJavadocTest.class)
        .addSourceLines(
            "impl/ImplJavadocPositiveCases.java",
            "/*",
            " * Copyright The OpenTelemetry Authors",
            " * SPDX-License-Identifier: Apache-2.0",
            " */",
            "package io.opentelemetry.gradle.customchecks.impl;",
            "// BUG: Diagnostic contains: missing the required javadoc disclaimer",
            "public class ImplJavadocPositiveCases {",
            "  // BUG: Diagnostic contains: missing the required javadoc disclaimer",
            "  public static class One {}",
            "  /** Doesn't have the disclaimer. */",
            "  // BUG: Diagnostic contains: missing the required javadoc disclaimer",
            "  public static class Two {}",
            "}")
        .doTest();
  }

  @Test
  void negativeCases() {
    CompilationTestHelper.newInstance(OtelImplJavadoc.class, OtelImplJavadocTest.class)
        .addSourceLines(
            "impl/ImplJavadocNegativeCases.java",
            "/*",
            " * Copyright The OpenTelemetry Authors",
            " * SPDX-License-Identifier: Apache-2.0",
            " */",
            "package io.opentelemetry.gradle.customchecks.impl;",
            "/**",
            " * This class is not intended for use by application developers. Its API is stable and",
            " * will not be changed or removed in a backwards-incompatible manner.",
            " */",
            "public class ImplJavadocNegativeCases {",
            "  /**",
            "   * This class is not intended for use by application developers. Its API is stable",
            "   * and will not be changed or removed in a backwards-incompatible manner.",
            "   */",
            "  public static class One {}",
            "  // Non-public class without disclaimer is fine.",
            "  static class Two {}",
            "}")
        .doTest();
  }

  @Test
  void nonImplPackageIgnored() {
    CompilationTestHelper.newInstance(OtelImplJavadoc.class, OtelImplJavadocTest.class)
        .addSourceLines(
            "other/NonImplPackageCases.java",
            "/*",
            " * Copyright The OpenTelemetry Authors",
            " * SPDX-License-Identifier: Apache-2.0",
            " */",
            "package io.opentelemetry.gradle.customchecks.other;",
            "public class NonImplPackageCases {",
            "  public static class One {}",
            "}")
        .doTest();
  }
}
