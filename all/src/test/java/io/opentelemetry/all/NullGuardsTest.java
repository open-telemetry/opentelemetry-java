/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.common.impl.ApiUsageLogger;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Structural test that public API entry points guard non-{@code @Nullable} reference parameters
 * against {@code null}, per {@code docs/knowledge/api-design.md#null-guards}.
 *
 * <p>Each method is classified as either {@code RUNTIME} (needs {@code ApiUsageLogger}-style
 * graceful degradation) or {@code FAIL_FAST} (needs {@code Objects.requireNonNull}):
 *
 * <ol>
 *   <li>Default per artifact: {@code opentelemetry-api*} and {@code opentelemetry-context} default
 *       to {@code RUNTIME}, everything else to {@code FAIL_FAST}.
 *   <li>Structural override: any method that overrides or implements a method declared on a
 *       supertype whose package starts with {@code io.opentelemetry.api.} or {@code
 *       io.opentelemetry.context.} is classified {@code RUNTIME}. This picks up SDK
 *       implementations of runtime API types ({@code SdkSpan}, {@code SdkLongCounter}, ...)
 *       without an enumeration.
 *   <li>Explicit per-method overrides in {@link #METHOD_STYLE_OVERRIDES} handle the small set of
 *       config-time methods on runtime-artifact classes (e.g. {@code GlobalOpenTelemetry.set}).
 * </ol>
 *
 * <p>Detection is intentionally lenient: it looks for the guard pattern anywhere in the method
 * body, not just at the top. This trades a small false-negative rate for far fewer false
 * positives while a module is being brought into compliance.
 */
@SuppressLogger(NullGuardsTest.class)
class NullGuardsTest {

  private static final Logger logger = Logger.getLogger(NullGuardsTest.class.getName());

  private static final String OTEL_BASE_PACKAGE = "io.opentelemetry";
  private static final String EQUALS_DESC =
      Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class));
  private static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

  /**
   * Runtime API scope: the artifacts and packages that constitute the runtime instrumentation
   * API. Keys are artifact base names; values are the corresponding base packages. Both are
   * matched as "equal-to or has-{@code -} / has-{@code .} suffix", so {@code opentelemetry-api}
   * covers {@code opentelemetry-api-incubator} and {@code io.opentelemetry.api} covers
   * {@code io.opentelemetry.api.trace} etc.
   *
   * <p>Methods in these artifacts, and methods anywhere that override methods declared in these
   * packages, default to {@code RUNTIME} null-guard semantics. Everything else defaults to
   * {@code FAIL_FAST}.
   */
  private static final Map<String, String> RUNTIME_SCOPE =
      Map.of(
          "opentelemetry-api", "io.opentelemetry.api",
          "opentelemetry-context", "io.opentelemetry.context");

  /**
   * Per-method style overrides for the small set of config-time methods that live on classes in
   * runtime-defaulted artifacts.
   */
  private static final Map<String, GuardStyle> METHOD_STYLE_OVERRIDES =
      Map.of(
          overrideKey(GlobalOpenTelemetry.class, "set", void.class, OpenTelemetry.class),
              GuardStyle.FAIL_FAST,
          overrideKey(GlobalOpenTelemetry.class, "set", void.class, Supplier.class),
              GuardStyle.FAIL_FAST,
          overrideKey(
                  OpenTelemetry.class,
                  "propagating",
                  OpenTelemetry.class,
                  ContextPropagators.class),
              GuardStyle.FAIL_FAST);

  private static String overrideKey(
      Class<?> owner, String name, Class<?> returnType, Class<?>... paramTypes) {
    Type[] params = Arrays.stream(paramTypes).map(Type::getType).toArray(Type[]::new);
    return Type.getInternalName(owner)
        + "#"
        + name
        + Type.getMethodDescriptor(Type.getType(returnType), params);
  }

  /**
   * Artifacts that have not yet been audited for null-guard compliance. Remove an entry once a
   * per-module PR has landed all guards for it. The sample module ({@code opentelemetry-api}) is
   * intentionally absent from this list.
   */
  private static final Set<String> exemptions =
      Set.of(
          "opentelemetry-api-incubator",
          "opentelemetry-context",
          "opentelemetry-common",
          "opentelemetry-exporter-common",
          "opentelemetry-exporter-logging",
          "opentelemetry-exporter-logging-otlp",
          "opentelemetry-exporter-otlp",
          "opentelemetry-exporter-otlp-common",
          "opentelemetry-exporter-otlp-profiles",
          "opentelemetry-exporter-prometheus",
          "opentelemetry-exporter-sender-grpc-managed-channel",
          "opentelemetry-exporter-sender-jdk",
          "opentelemetry-exporter-sender-okhttp",
          "opentelemetry-extension-kotlin",
          "opentelemetry-extension-trace-propagators",
          "opentelemetry-opencensus-shim",
          "opentelemetry-opentracing-shim",
          "opentelemetry-opentelemetry-jfr-profiles-shim",
          "opentelemetry-sdk",
          "opentelemetry-sdk-common",
          "opentelemetry-sdk-logs",
          "opentelemetry-sdk-metrics",
          "opentelemetry-sdk-profiles",
          "opentelemetry-sdk-testing",
          "opentelemetry-sdk-trace",
          "opentelemetry-sdk-extension-autoconfigure",
          "opentelemetry-sdk-extension-autoconfigure-spi",
          "opentelemetry-sdk-extension-declarative-config",
          "opentelemetry-sdk-extension-incubator",
          "opentelemetry-sdk-extension-jaeger-remote-sampler");

  enum GuardStyle {
    RUNTIME, // ApiUsageLogger + graceful degradation
    FAIL_FAST // Objects.requireNonNull
  }

  /** Cross-artifact registry loaded once. Key is internal class name (slashes). */
  private static Map<String, ClassNode> classRegistry;

  @BeforeAll
  static void loadClassRegistry() throws IOException {
    if (classRegistry != null) {
      return;
    }
    Map<String, ClassNode> registry = new HashMap<>();
    for (String[] entry : readArtifactsAndJars()) {
      loadJarInto(entry[1], registry);
    }
    classRegistry = registry;
  }

  @ParameterizedTest
  @MethodSource("artifactsAndJars")
  void nullGuards(String artifactId, String absolutePath) throws IOException {
    Map<String, ClassNode> artifactClasses = new LinkedHashMap<>();
    loadJarInto(absolutePath, artifactClasses);
    GuardStyle artifactDefault = defaultStyleFor(artifactId);

    List<Finding> findings = new ArrayList<>();
    for (ClassNode cls : artifactClasses.values()) {
      if (!isInAuditablePackage(cls)) {
        continue;
      }
      for (MethodNode method : cls.methods) {
        if (!isMethodPubliclyReachable(cls, method)) {
          continue;
        }
        GuardStyle style = guardStyleFor(cls, method, artifactDefault);
        findGuardViolations(cls, method, style, findings);
      }
    }

    try {
      if (!findings.isEmpty()) {
        throw new AssertionError(
            artifactId + " has null-guard findings:\n" + renderReport(artifactId, findings));
      }
      logger.log(Level.INFO, artifactId + " is null-guard compliant");
    } catch (AssertionError e) {
      if (exemptions.contains(artifactId)) {
        // To view details, remove from exemptions list.
        logger.log(
            Level.WARNING, artifactId + " has null-guard findings but is temporarily exempt");
      } else {
        throw e;
      }
    }
  }

  private static void loadJarInto(String jarPath, Map<String, ClassNode> out) throws IOException {
    try (JarFile jar = new JarFile(new File(jarPath))) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (!name.endsWith(".class") || name.startsWith("META-INF/")) {
          continue;
        }
        try (InputStream in = jar.getInputStream(entry)) {
          ClassReader reader = new ClassReader(in);
          ClassNode node = new ClassNode();
          reader.accept(node, ClassReader.SKIP_FRAMES);
          out.put(node.name, node);
        }
      }
    }
  }

  private static GuardStyle defaultStyleFor(String artifactId) {
    return isRuntimeApiArtifact(artifactId) ? GuardStyle.RUNTIME : GuardStyle.FAIL_FAST;
  }

  private static boolean isRuntimeApiArtifact(String artifactId) {
    return RUNTIME_SCOPE.keySet().stream()
        .anyMatch(base -> artifactId.equals(base) || artifactId.startsWith(base + "-"));
  }

  private static boolean isRuntimeApiPackage(String internalName) {
    String pkg = packageOf(internalName);
    return RUNTIME_SCOPE.values().stream()
        .anyMatch(base -> pkg.equals(base) || pkg.startsWith(base + "."));
  }

  private static GuardStyle guardStyleFor(
      ClassNode cls, MethodNode method, GuardStyle artifactDefault) {
    String key = cls.name + "#" + method.name + method.desc;
    GuardStyle explicit = METHOD_STYLE_OVERRIDES.get(key);
    if (explicit != null) {
      return explicit;
    }
    if (overridesRuntimeApiMethod(cls, method)) {
      return GuardStyle.RUNTIME;
    }
    return artifactDefault;
  }

  /**
   * True iff {@code method} overrides or implements a method with the same name+descriptor
   * declared on any supertype whose package is under {@code io.opentelemetry.api.} or {@code
   * io.opentelemetry.context.}. Static and private methods cannot override.
   */
  private static boolean overridesRuntimeApiMethod(ClassNode cls, MethodNode method) {
    if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) != 0) {
      return false;
    }
    if ("<init>".equals(method.name) || "<clinit>".equals(method.name)) {
      return false;
    }
    return anySupertypeMatches(
        cls,
        sup -> isRuntimeApiPackage(sup.name) && declaresMethod(sup, method.name, method.desc));
  }

  /**
   * Walks {@code cls}'s superclass chain and interface graph (not including {@code cls} itself),
   * returning true if {@code match} accepts any visited supertype. Cycle-safe.
   */
  private static boolean anySupertypeMatches(ClassNode cls, Predicate<ClassNode> match) {
    Set<String> visited = new LinkedHashSet<>();
    visited.add(cls.name);
    return walkSupertypes(cls, visited, match);
  }

  private static boolean walkSupertypes(
      ClassNode cls, Set<String> visited, Predicate<ClassNode> match) {
    if (cls.superName != null && !OBJECT_INTERNAL_NAME.equals(cls.superName)) {
      if (visitSupertype(classRegistry.get(cls.superName), visited, match)) {
        return true;
      }
    }
    if (cls.interfaces != null) {
      for (String iface : cls.interfaces) {
        if (visitSupertype(classRegistry.get(iface), visited, match)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean visitSupertype(
      ClassNode cls, Set<String> visited, Predicate<ClassNode> match) {
    if (cls == null || !visited.add(cls.name)) {
      return false;
    }
    if (match.test(cls)) {
      return true;
    }
    return walkSupertypes(cls, visited, match);
  }

  private static boolean declaresMethod(ClassNode cls, String name, String desc) {
    return cls.methods.stream()
        .filter(m -> (m.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) == 0)
        .anyMatch(m -> name.equals(m.name) && desc.equals(m.desc));
  }

  private static boolean isInAuditablePackage(ClassNode cls) {
    if ((cls.access & Opcodes.ACC_SYNTHETIC) != 0) {
      return false;
    }
    if (isAutoValueGenerated(cls)) {
      return false;
    }
    return isAuditableOtelPackage(cls.name);
  }

  /** Auto-generated; not editable. */
  private static boolean isAutoValueGenerated(ClassNode cls) {
    int lastSep = Math.max(cls.name.lastIndexOf('/'), cls.name.lastIndexOf('$'));
    return cls.name.startsWith("AutoValue_", lastSep + 1);
  }

  /**
   * True iff a caller in another package can dispatch to this method via some publicly-visible
   * reference type. Either (a) the declaring class is itself public, or (b) the method overrides
   * a method declared on a public supertype in a non-internal {@code io.opentelemetry.*} package
   * (typical for package-private SDK impls of API interfaces like {@code SdkSpan implements
   * Span}).
   */
  private static boolean isMethodPubliclyReachable(ClassNode cls, MethodNode method) {
    if ((method.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0) {
      return false;
    }
    if ((cls.access & Opcodes.ACC_PUBLIC) != 0) {
      return true;
    }
    if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) != 0) {
      return false;
    }
    if ("<init>".equals(method.name) || "<clinit>".equals(method.name)) {
      return false;
    }
    return anySupertypeMatches(
        cls,
        sup ->
            (sup.access & Opcodes.ACC_PUBLIC) != 0
                && isAuditableOtelPackage(sup.name)
                && declaresMethod(sup, method.name, method.desc));
  }

  private static boolean isAuditableOtelPackage(String internalName) {
    String pkg = packageOf(internalName);
    if (!pkg.startsWith(OTEL_BASE_PACKAGE)) {
      return false;
    }
    return !pkg.contains(".internal") && !pkg.endsWith(".impl") && !pkg.contains(".impl.");
  }

  private static void findGuardViolations(
      ClassNode cls, MethodNode method, GuardStyle style, List<Finding> findings) {
    if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE))
        != 0) {
      return;
    }
    if (isKnownNullAcceptingSignature(method)) {
      return;
    }
    Type[] paramTypes = Type.getMethodType(method.desc).getArgumentTypes();
    if (paramTypes.length == 0) {
      return;
    }

    boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
    int slot = isStatic ? 0 : 1;
    for (int i = 0; i < paramTypes.length; i++) {
      Type pt = paramTypes[i];
      int paramSlot = slot;
      int paramIndex = i;
      slot += pt.getSize();
      if (pt.getSort() != Type.OBJECT && pt.getSort() != Type.ARRAY) {
        continue;
      }
      Consumer<String> report =
          msg -> findings.add(new Finding(cls.name, method.name, method.desc, paramIndex, msg));
      boolean nullable = isParamNullable(method, paramIndex);
      boolean requireNonNullGuard = GuardScanner.hasRequireNonNullGuard(method, paramSlot);
      boolean apiUsageLoggerGuard = GuardScanner.hasApiUsageLoggerGuard(method, paramSlot);

      if (nullable) {
        if (requireNonNullGuard) {
          report.accept("@Nullable parameter is guarded with Objects.requireNonNull");
        }
        continue;
      }

      boolean hasAnyGuard = requireNonNullGuard || apiUsageLoggerGuard;
      if (!hasAnyGuard) {
        report.accept(
            "missing null guard ("
                + (style == GuardStyle.RUNTIME
                    ? "expected ApiUsageLogger.logNullParam"
                    : "expected Objects.requireNonNull")
                + ")");
        continue;
      }
      if (style == GuardStyle.RUNTIME && requireNonNullGuard && !apiUsageLoggerGuard) {
        report.accept(
            "runtime API uses Objects.requireNonNull; expected ApiUsageLogger.logNullParam");
      } else if (style == GuardStyle.FAIL_FAST && apiUsageLoggerGuard && !requireNonNullGuard) {
        report.accept(
            "fail-fast API uses ApiUsageLogger; expected Objects.requireNonNull");
      }
    }
  }

  private static boolean isKnownNullAcceptingSignature(MethodNode method) {
    return "equals".equals(method.name) && EQUALS_DESC.equals(method.desc);
  }

  private static boolean isParamNullable(MethodNode method, int paramIndex) {
    if (containsNullable(method.visibleParameterAnnotations, paramIndex)) {
      return true;
    }
    return containsNullable(method.invisibleParameterAnnotations, paramIndex);
  }

  private static boolean containsNullable(List<AnnotationNode>[] paramAnnos, int paramIndex) {
    if (paramAnnos == null || paramIndex >= paramAnnos.length || paramAnnos[paramIndex] == null) {
      return false;
    }
    return paramAnnos[paramIndex].stream()
        .anyMatch(a -> a.desc != null && a.desc.endsWith("/Nullable;"));
  }

  /** Bytecode-level detection of the two null-guard shapes on a given parameter slot. */
  private static final class GuardScanner {

    private static final String OBJECTS = Type.getInternalName(Objects.class);
    private static final String API_USAGE_LOGGER = Type.getInternalName(ApiUsageLogger.class);

    private GuardScanner() {}

    /** True iff {@code paramSlot} is passed as the first arg to {@code Objects.requireNonNull}. */
    static boolean hasRequireNonNullGuard(MethodNode method, int paramSlot) {
      for (AbstractInsnNode insn = method.instructions.getFirst();
          insn != null;
          insn = insn.getNext()) {
        if (!(insn instanceof MethodInsnNode)) {
          continue;
        }
        MethodInsnNode mi = (MethodInsnNode) insn;
        if (mi.getOpcode() != Opcodes.INVOKESTATIC
            || !OBJECTS.equals(mi.owner)
            || !"requireNonNull".equals(mi.name)) {
          continue;
        }
        AbstractInsnNode prev = previousInsn(mi.getPrevious());
        // For 2-arg overloads, skip past the trailing message arg to reach the receiver.
        if (Type.getArgumentTypes(mi.desc).length == 2) {
          prev = prev == null ? null : previousInsn(prev.getPrevious());
        }
        if (prev instanceof VarInsnNode) {
          VarInsnNode v = (VarInsnNode) prev;
          if (v.getOpcode() == Opcodes.ALOAD && v.var == paramSlot) {
            return true;
          }
        }
      }
      return false;
    }

    /**
     * True iff the method contains {@code if (paramSlot == null) { ... ApiUsageLogger.log*(...);
     * ...; return; }}: an {@code IFNONNULL} on the param whose fall-through branch contains
     * a call to {@code ApiUsageLogger} before the jump target.
     */
    static boolean hasApiUsageLoggerGuard(MethodNode method, int paramSlot) {
      for (AbstractInsnNode insn = method.instructions.getFirst();
          insn != null;
          insn = insn.getNext()) {
        if (!(insn instanceof VarInsnNode)) {
          continue;
        }
        VarInsnNode v = (VarInsnNode) insn;
        if (v.getOpcode() != Opcodes.ALOAD || v.var != paramSlot) {
          continue;
        }
        AbstractInsnNode next = nextInsn(v.getNext());
        if (!(next instanceof JumpInsnNode)) {
          continue;
        }
        JumpInsnNode jump = (JumpInsnNode) next;
        if (jump.getOpcode() != Opcodes.IFNONNULL) {
          continue;
        }
        AbstractInsnNode cursor = jump.getNext();
        while (cursor != null && !cursor.equals(jump.label)) {
          if (cursor instanceof MethodInsnNode
              && API_USAGE_LOGGER.equals(((MethodInsnNode) cursor).owner)) {
            return true;
          }
          cursor = cursor.getNext();
        }
      }
      return false;
    }

    /** Walk back over ASM meta nodes (labels, line numbers, frames) to the previous real insn. */
    private static AbstractInsnNode previousInsn(AbstractInsnNode insn) {
      while (insn != null && isMeta(insn)) {
        insn = insn.getPrevious();
      }
      return insn;
    }

    /** Walk forward over ASM meta nodes to the next real insn. */
    private static AbstractInsnNode nextInsn(AbstractInsnNode insn) {
      while (insn != null && isMeta(insn)) {
        insn = insn.getNext();
      }
      return insn;
    }

    private static boolean isMeta(AbstractInsnNode insn) {
      int t = insn.getType();
      return t == AbstractInsnNode.LABEL
          || t == AbstractInsnNode.LINE
          || t == AbstractInsnNode.FRAME;
    }
  }

  private static String renderReport(String artifactId, List<Finding> findings) {
    Map<String, List<Finding>> byClass =
        findings.stream()
            .collect(
                Collectors.groupingBy(f -> f.className, LinkedHashMap::new, Collectors.toList()));
    StringBuilder sb = new StringBuilder();
    sb.append("Artifact: ")
        .append(artifactId)
        .append("  (")
        .append(findings.size())
        .append(" findings)\n");
    for (Map.Entry<String, List<Finding>> e : byClass.entrySet()) {
      sb.append("  ").append(e.getKey().replace('/', '.')).append('\n');
      for (Finding f : e.getValue()) {
        sb.append("    ")
            .append(f.methodName)
            .append(f.methodDesc)
            .append(" [param #")
            .append(f.paramIndex)
            .append("] ")
            .append(f.message)
            .append('\n');
      }
    }
    return sb.toString();
  }

  private static String packageOf(String internalName) {
    int slash = internalName.lastIndexOf('/');
    return slash < 0 ? "" : internalName.substring(0, slash).replace('/', '.');
  }

  private static List<String[]> readArtifactsAndJars() throws IOException {
    List<String> lines = Files.readAllLines(Path.of(System.getenv("ARTIFACTS_AND_JARS")));
    List<String[]> out = new ArrayList<>();
    for (String line : lines) {
      out.add(line.split(":", 2));
    }
    return out;
  }

  private static Stream<Arguments> artifactsAndJars() throws IOException {
    return readArtifactsAndJars().stream()
        .map(parts -> Arguments.argumentSet(parts[0], parts[0], parts[1]));
  }

  private static final class Finding {
    final String className;
    final String methodName;
    final String methodDesc;
    final int paramIndex;
    final String message;

    Finding(
        String className, String methodName, String methodDesc, int paramIndex, String message) {
      this.className = className;
      this.methodName = methodName;
      this.methodDesc = methodDesc;
      this.paramIndex = paramIndex;
      this.message = message;
    }
  }
}
