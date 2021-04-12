/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.base.Optional;
import com.tngtech.archunit.base.PackageMatcher;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClassList;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.lang.syntax.elements.MethodsShouldConjunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SdkDesignTest {

  // TODO: limited to trace as currently metrics fails the test (SdkMeterProvider returns SdkMeter)
  private static final JavaClasses SDK_OTEL_CLASSES =
      new ClassFileImporter().importPackages("io.opentelemetry.sdk.trace");

  /**
   * Ensures that all SDK methods that: - are defined in classes that extend or implement API model
   * and are public (to exclude protected builders) - are public (avoids issues with protected
   * methods returning classes unavailable to test's CL) - override or implement parent method
   * return only API, Context or generic Java type
   */
  @Test
  void sdkImplementationOfApiClassesShouldReturnApiTypeOnly() {
    MethodsShouldConjunction covariantReturnRule =
        ArchRuleDefinition.methods()
            .that()
            .areDeclaredInClassesThat()
            .areAssignableTo(inPackage("io.opentelemetry.api.."))
            .and()
            .areDeclaredInClassesThat()
            .arePublic()
            .and()
            .arePublic()
            .and(implmentOrOverride())
            .should()
            .haveRawReturnType(
                inPackage("io.opentelemetry.api..", "io.opentelemetry.context..", "java.."))
            .orShould()
            .haveRawReturnType("void");

    covariantReturnRule.check(SDK_OTEL_CLASSES);
  }

  static DescribedPredicate<? super JavaMethod> implmentOrOverride() {
    return new DescribedPredicate<>("implement or override a method") {
      @Override
      public boolean apply(JavaMethod input) {
        JavaClassList params = input.getRawParameterTypes();
        Class<?>[] paramsType = new Class<?>[params.size()];
        for (int i = 0, n = params.size(); i < n; i++) {
          paramsType[i] = params.get(i).reflect();
        }
        String name = input.getName();

        List<JavaClass> parents = new ArrayList<>(input.getOwner().getAllRawSuperclasses());
        parents.addAll(input.getOwner().getAllInterfaces());

        for (JavaClass parent : parents) {
          Optional<JavaMethod> found = parent.tryGetMethod(name, paramsType);
          if (found.isPresent()) {
            return true;
          }
        }
        return false;
      }
    };
  }

  static DescribedPredicate<? super JavaClass> inPackage(String... requiredPackages) {
    return new DescribedPredicate<>("are in " + Arrays.toString(requiredPackages)) {
      @Override
      public boolean apply(JavaClass member) {
        for (String requiredPackage : requiredPackages) {
          if (PackageMatcher.of(requiredPackage).matches(member.getPackageName())) {
            return true;
          }
        }
        return false;
      }
    };
  }
}
