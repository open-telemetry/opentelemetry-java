/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JPackage;
import org.jsonschema2pojo.rules.RuleFactory;

/**
 * Routes generated types whose name starts with {@code Experimental} into an {@code internal}
 * sub-package.
 *
 * <p>Declarative config exposes both stable and unstable (experimental) model types. Unstable types
 * are subject to breaking changes in minor versions, so they must live in an {@code internal}
 * package that is exempt from the stability guarantees in {@code VERSIONING.md}. Experimental types
 * are identified by the {@code Experimental} prefix, which is derived from the schema {@code title}
 * (see {@code useTitleAsClassname} in {@code build.gradle.kts}).
 *
 * <p>jsonschema2pojo generates every type into a single {@code targetPackage}. Both {@link
 * OtelObjectRule} and {@link OtelEnumRule} consult this helper before delegating to the stock rule
 * so the type is created in the correct package. Because the type is created in its final package,
 * codemodel resolves cross-package references and emits the necessary imports automatically.
 */
final class ExperimentalPackages {

  private static final String EXPERIMENTAL_PREFIX = "Experimental";
  private static final String INTERNAL_SUBPACKAGE = "internal";

  private ExperimentalPackages() {}

  /**
   * Returns the package the type generated for {@code node} should be created in: the {@code
   * internal} sub-package for experimental types, the model root for everything else.
   *
   * <p>The routing is symmetric so that the package is determined purely by the {@code Experimental}
   * name prefix, regardless of the incoming {@code pkg}. This matters because jsonschema2pojo
   * creates {@code $ref} targets in the referrer's package context: a shared stable type (e.g. one
   * without a {@code title}, such as {@code SpanKind}) that is first referenced from inside an
   * experimental type would otherwise be pulled into {@code internal}. Forcing non-experimental
   * types back out keeps them in the public model package.
   */
  static JPackage resolve(RuleFactory ruleFactory, String nodeName, JsonNode node, JPackage pkg) {
    // getClassName applies the configured prefix/suffix and resolves the title-based name. The
    // Model suffix differs for enums, but the Experimental prefix (derived from the title or the
    // $def name) is present either way, which is all that matters for the routing decision.
    String className = ruleFactory.getNameHelper().getClassName(nodeName, node, pkg);
    boolean experimental = className.startsWith(EXPERIMENTAL_PREFIX);
    boolean inInternal = pkg.name().endsWith("." + INTERNAL_SUBPACKAGE);
    JPackage root = inInternal ? pkg.parent() : pkg;
    return experimental ? root.subPackage(INTERNAL_SUBPACKAGE) : root;
  }
}
