/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.customchecks;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.PackageTree;
import com.sun.tools.javac.api.JavacTrees;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;

@BugPattern(
    summary =
        "This public impl class is missing the required javadoc disclaimer: \""
            + OtelImplJavadoc.EXPECTED_IMPL_COMMENT
            + "\"",
    severity = WARNING,
    linkType = NONE)
public class OtelImplJavadoc extends BugChecker implements BugChecker.ClassTreeMatcher {

  private static final long serialVersionUID = 1L;

  private static final Pattern IMPL_PACKAGE_PATTERN = Pattern.compile("\\bimpl\\b");

  static final String EXPECTED_IMPL_COMMENT =
      "This class is not intended for use by application developers."
          + " Its API is stable and will not be changed or removed in a backwards-incompatible"
          + " manner.";

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!isPublic(tree) || !isImpl(state) || tree.getSimpleName().toString().endsWith("Test")) {
      return Description.NO_MATCH;
    }
    String javadoc = getJavadoc(state);
    if (javadoc != null && javadoc.contains(EXPECTED_IMPL_COMMENT)) {
      return Description.NO_MATCH;
    }
    return describeMatch(tree);
  }

  private static boolean isPublic(ClassTree tree) {
    return tree.getModifiers().getFlags().contains(Modifier.PUBLIC);
  }

  private static boolean isImpl(VisitorState state) {
    PackageTree packageTree = state.getPath().getCompilationUnit().getPackage();
    if (packageTree == null) {
      return false;
    }
    String packageName = state.getSourceForNode(packageTree.getPackageName());
    return packageName != null && IMPL_PACKAGE_PATTERN.matcher(packageName).find();
  }

  @Nullable
  private static String getJavadoc(VisitorState state) {
    DocCommentTree docCommentTree =
        JavacTrees.instance(state.context).getDocCommentTree(state.getPath());
    if (docCommentTree == null) {
      return null;
    }
    return docCommentTree.toString().replace("\n", " ").replace(" * ", " ").replaceAll("\\s+", " ");
  }
}
