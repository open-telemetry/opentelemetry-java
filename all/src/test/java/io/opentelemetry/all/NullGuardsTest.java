/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.common.impl.ApiUsageLogger;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.Annotation;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LabelTarget;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
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
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
 *       io.opentelemetry.context.} is classified {@code RUNTIME}. This picks up SDK implementations
 *       of runtime API types ({@code SdkSpan}, {@code SdkLongCounter}, ...) without an enumeration.
 *   <li>Explicit per-method overrides in {@link #METHOD_STYLE_OVERRIDES} handle config-time methods
 *       and runtime methods without type-safe fallbacks.
 * </ol>
 *
 * <p>Detection is intentionally lenient: it looks for the guard pattern anywhere in the method
 * body, not just at the top. This trades a small false-negative rate for far fewer false positives
 * while a module is being brought into compliance.
 */
@SuppressLogger(NullGuardsTest.class)
class NullGuardsTest {

  private static final Logger logger = Logger.getLogger(NullGuardsTest.class.getName());

  private static final String OTEL_BASE_PACKAGE = "io.opentelemetry";
  private static final String EQUALS_DESC =
      MethodTypeDesc.of(ClassDesc.ofDescriptor("Z"), ClassDesc.of("java.lang.Object"))
          .descriptorString();
  private static final String OBJECT_INTERNAL_NAME = Object.class.getName().replace('.', '/');

  /**
   * Runtime API scope: the artifacts and packages that constitute the runtime instrumentation API.
   * Keys are artifact base names; values are the corresponding base packages. Both are matched as
   * "equal-to or has-{@code -} / has-{@code .} suffix", so {@code opentelemetry-api} covers {@code
   * opentelemetry-api-incubator} and {@code io.opentelemetry.api} covers {@code
   * io.opentelemetry.api.trace} etc.
   *
   * <p>Methods in these artifacts, and methods anywhere that override methods declared in these
   * packages, default to {@code RUNTIME} null-guard semantics. Everything else defaults to {@code
   * FAIL_FAST}.
   */
  private static final Map<String, String> RUNTIME_SCOPE =
      Map.of(
          "opentelemetry-api", "io.opentelemetry.api",
          "opentelemetry-context", "io.opentelemetry.context");

  /**
   * Per-method style overrides for config-time methods and runtime methods without type-safe
   * fallbacks in runtime-defaulted artifacts.
   */
  private static final Map<String, GuardStyle> METHOD_STYLE_OVERRIDES =
      Map.ofEntries(
          Map.entry(
              overrideKey(GlobalOpenTelemetry.class, "set", void.class, OpenTelemetry.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(GlobalOpenTelemetry.class, "set", void.class, Supplier.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(
                  OpenTelemetry.class,
                  "propagating",
                  OpenTelemetry.class,
                  ContextPropagators.class),
              GuardStyle.FAIL_FAST),
          // Context wrappers are setup-time adapters. Fail fast rather than defer an NPE until use
          // or substitute a noop that could discard application work or return an invalid result.
          Map.entry(
              overrideKey(Context.class, "taskWrapping", Executor.class, Executor.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(
                  Context.class, "taskWrapping", ExecutorService.class, ExecutorService.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(
                  Context.class,
                  "taskWrapping",
                  ScheduledExecutorService.class,
                  ScheduledExecutorService.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrap", Executor.class, Executor.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrap", ExecutorService.class, ExecutorService.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(
                  Context.class,
                  "wrap",
                  ScheduledExecutorService.class,
                  ScheduledExecutorService.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrap", Runnable.class, Runnable.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrap", Callable.class, Callable.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrapFunction", Function.class, Function.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrapFunction", BiFunction.class, BiFunction.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrapConsumer", Consumer.class, Consumer.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrapConsumer", BiConsumer.class, BiConsumer.class),
              GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Context.class, "wrapSupplier", Supplier.class, Supplier.class),
              GuardStyle.FAIL_FAST),
          // Value.empty() cannot preserve these generic return types. We may reevaluate in future.
          Map.entry(
              overrideKey(Value.class, "of", Value.class, String.class), GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Value.class, "of", Value.class, byte[].class), GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Value.class, "of", Value.class, Value[].class), GuardStyle.FAIL_FAST),
          Map.entry(overrideKey(Value.class, "of", Value.class, List.class), GuardStyle.FAIL_FAST),
          Map.entry(
              overrideKey(Value.class, "of", Value.class, KeyValue[].class), GuardStyle.FAIL_FAST),
          Map.entry(overrideKey(Value.class, "of", Value.class, Map.class), GuardStyle.FAIL_FAST));

  private static String overrideKey(
      Class<?> owner, String name, Class<?> returnType, Class<?>... paramTypes) {
    ClassDesc[] params =
        Arrays.stream(paramTypes)
            .map(type -> ClassDesc.ofDescriptor(type.descriptorString()))
            .toArray(ClassDesc[]::new);
    return owner.getName().replace('.', '/')
        + "#"
        + name
        + ":"
        + MethodTypeDesc.of(ClassDesc.ofDescriptor(returnType.descriptorString()), params)
            .descriptorString();
  }

  /**
   * Artifacts that have not yet been audited for null-guard compliance. Remove an entry once a
   * per-module PR has landed all guards for it.
   */
  private static final Set<String> exemptions =
      Set.of(
          "opentelemetry-api",
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
  private static Map<String, ClassModel> classRegistry;

  @BeforeAll
  static void loadClassRegistry() throws IOException {
    if (classRegistry != null) {
      return;
    }
    Map<String, ClassModel> registry = new HashMap<>();
    for (String[] entry : readArtifactsAndJars()) {
      loadJarInto(entry[1], registry);
    }
    classRegistry = registry;
  }

  @ParameterizedTest
  @MethodSource("artifactsAndJars")
  void nullGuards(String artifactId, String absolutePath) throws IOException {
    Map<String, ClassModel> artifactClasses = new LinkedHashMap<>();
    loadJarInto(absolutePath, artifactClasses);
    GuardStyle artifactDefault = defaultStyleFor(artifactId);

    List<Finding> findings = new ArrayList<>();
    for (ClassModel cls : artifactClasses.values()) {
      if (!isInAuditablePackage(cls)) {
        continue;
      }
      for (MethodModel method : cls.methods()) {
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

  private static void loadJarInto(String jarPath, Map<String, ClassModel> out) throws IOException {
    try (JarFile jar = new JarFile(new File(jarPath))) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (!name.endsWith(".class") || name.startsWith("META-INF/")) {
          continue;
        }
        try (InputStream in = jar.getInputStream(entry)) {
          ClassModel model = ClassFile.of().parse(in.readAllBytes());
          out.put(model.thisClass().asInternalName(), model);
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
      ClassModel cls, MethodModel method, GuardStyle artifactDefault) {
    String key =
        cls.thisClass().asInternalName()
            + "#"
            + method.methodName().stringValue()
            + ":"
            + method.methodType().stringValue();
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
   * True iff {@code method} overrides or implements a method with the same name+descriptor declared
   * on any supertype whose package is under {@code io.opentelemetry.api.} or {@code
   * io.opentelemetry.context.}. Static and private methods cannot override.
   */
  private static boolean overridesRuntimeApiMethod(ClassModel cls, MethodModel method) {
    if (method.flags().has(AccessFlag.STATIC) || method.flags().has(AccessFlag.PRIVATE)) {
      return false;
    }
    String name = method.methodName().stringValue();
    if ("<init>".equals(name) || "<clinit>".equals(name)) {
      return false;
    }
    return anySupertypeMatches(
        cls,
        sup ->
            isRuntimeApiPackage(sup.thisClass().asInternalName())
                && declaresMethod(sup, name, method.methodType().stringValue()));
  }

  /**
   * Walks {@code cls}'s superclass chain and interface graph (not including {@code cls} itself),
   * returning true if {@code match} accepts any visited supertype. Cycle-safe.
   */
  private static boolean anySupertypeMatches(ClassModel cls, Predicate<ClassModel> match) {
    Set<String> visited = new LinkedHashSet<>();
    visited.add(cls.thisClass().asInternalName());
    return walkSupertypes(cls, visited, match);
  }

  private static boolean walkSupertypes(
      ClassModel cls, Set<String> visited, Predicate<ClassModel> match) {
    if (cls.superclass().isPresent()) {
      String superName = cls.superclass().get().asInternalName();
      if (!OBJECT_INTERNAL_NAME.equals(superName)
          && visitSupertype(classRegistry.get(superName), visited, match)) {
        return true;
      }
    }
    for (var iface : cls.interfaces()) {
      if (visitSupertype(classRegistry.get(iface.asInternalName()), visited, match)) {
        return true;
      }
    }
    return false;
  }

  private static boolean visitSupertype(
      ClassModel cls, Set<String> visited, Predicate<ClassModel> match) {
    if (cls == null || !visited.add(cls.thisClass().asInternalName())) {
      return false;
    }
    if (match.test(cls)) {
      return true;
    }
    return walkSupertypes(cls, visited, match);
  }

  private static boolean declaresMethod(ClassModel cls, String name, String desc) {
    return cls.methods().stream()
        .filter(m -> !m.flags().has(AccessFlag.STATIC) && !m.flags().has(AccessFlag.PRIVATE))
        .anyMatch(
            m ->
                name.equals(m.methodName().stringValue())
                    && desc.equals(m.methodType().stringValue()));
  }

  private static boolean isInAuditablePackage(ClassModel cls) {
    if (cls.flags().has(AccessFlag.SYNTHETIC)) {
      return false;
    }
    if (isAutoValueGenerated(cls)) {
      return false;
    }
    return isAuditableOtelPackage(cls.thisClass().asInternalName());
  }

  /** Auto-generated; not editable. */
  private static boolean isAutoValueGenerated(ClassModel cls) {
    String name = cls.thisClass().asInternalName();
    int lastSep = Math.max(name.lastIndexOf('/'), name.lastIndexOf('$'));
    return name.startsWith("AutoValue_", lastSep + 1);
  }

  /**
   * True iff a caller in another package can dispatch to this method via some publicly-visible
   * reference type. Either (a) the declaring class is itself public, or (b) the method overrides a
   * method declared on a public supertype in a non-internal {@code io.opentelemetry.*} package
   * (typical for package-private SDK impls of API interfaces like {@code SdkSpan implements Span}).
   */
  private static boolean isMethodPubliclyReachable(ClassModel cls, MethodModel method) {
    if (!method.flags().has(AccessFlag.PUBLIC) && !method.flags().has(AccessFlag.PROTECTED)) {
      return false;
    }
    if (cls.flags().has(AccessFlag.PUBLIC)) {
      return true;
    }
    if (method.flags().has(AccessFlag.STATIC) || method.flags().has(AccessFlag.PRIVATE)) {
      return false;
    }
    String name = method.methodName().stringValue();
    if ("<init>".equals(name) || "<clinit>".equals(name)) {
      return false;
    }
    return anySupertypeMatches(
        cls,
        sup ->
            sup.flags().has(AccessFlag.PUBLIC)
                && isAuditableOtelPackage(sup.thisClass().asInternalName())
                && declaresMethod(sup, name, method.methodType().stringValue()));
  }

  private static boolean isAuditableOtelPackage(String internalName) {
    String pkg = packageOf(internalName);
    if (!pkg.startsWith(OTEL_BASE_PACKAGE)) {
      return false;
    }
    return !pkg.contains(".internal") && !pkg.endsWith(".impl") && !pkg.contains(".impl.");
  }

  private static void findGuardViolations(
      ClassModel cls, MethodModel method, GuardStyle style, List<Finding> findings) {
    if (method.flags().has(AccessFlag.ABSTRACT)
        || method.flags().has(AccessFlag.SYNTHETIC)
        || method.flags().has(AccessFlag.BRIDGE)) {
      return;
    }
    if (isKnownNullAcceptingSignature(method)) {
      return;
    }
    List<ClassDesc> paramTypes = method.methodTypeSymbol().parameterList();
    if (paramTypes.isEmpty()) {
      return;
    }

    boolean isStatic = method.flags().has(AccessFlag.STATIC);
    int slot = isStatic ? 0 : 1;
    for (int i = 0; i < paramTypes.size(); i++) {
      ClassDesc pt = paramTypes.get(i);
      int paramSlot = slot;
      int paramIndex = i;
      slot += TypeKind.from(pt).slotSize();
      if (pt.isPrimitive()) {
        continue;
      }
      Consumer<String> report =
          msg ->
              findings.add(
                  new Finding(
                      cls.thisClass().asInternalName(),
                      method.methodName().stringValue(),
                      method.methodType().stringValue(),
                      paramIndex,
                      msg));
      boolean nullable = isParamNullable(method, paramIndex);
      boolean requireNonNullGuard = GuardScanner.hasRequireNonNullGuard(method, paramSlot);
      boolean apiUsageLoggerGuard = GuardScanner.hasApiUsageLoggerGuard(method, paramSlot);

      if (nullable) {
        if (requireNonNullGuard) {
          report.accept("@Nullable but guarded with requireNonNull (contradicts contract)");
        }
        continue;
      }

      boolean hasAnyGuard = requireNonNullGuard || apiUsageLoggerGuard;
      if (!hasAnyGuard) {
        report.accept(
            style == GuardStyle.RUNTIME
                ? "missing ApiUsageLogger.logNullParam guard"
                : "missing Objects.requireNonNull guard");
        continue;
      }
      if (style == GuardStyle.RUNTIME && requireNonNullGuard && !apiUsageLoggerGuard) {
        report.accept("wrong style: uses requireNonNull; expected ApiUsageLogger.logNullParam");
      } else if (style == GuardStyle.FAIL_FAST && apiUsageLoggerGuard && !requireNonNullGuard) {
        report.accept("wrong style: uses ApiUsageLogger; expected Objects.requireNonNull");
      }
    }
  }

  private static boolean isKnownNullAcceptingSignature(MethodModel method) {
    return "equals".equals(method.methodName().stringValue())
        && EQUALS_DESC.equals(method.methodType().stringValue());
  }

  private static boolean isParamNullable(MethodModel method, int paramIndex) {
    return method
            .findAttribute(Attributes.runtimeVisibleParameterAnnotations())
            .map(a -> containsNullable(a.parameterAnnotations(), paramIndex))
            .orElse(false)
        || method
            .findAttribute(Attributes.runtimeInvisibleParameterAnnotations())
            .map(a -> containsNullable(a.parameterAnnotations(), paramIndex))
            .orElse(false);
  }

  private static boolean containsNullable(List<List<Annotation>> paramAnnos, int paramIndex) {
    return paramIndex < paramAnnos.size()
        && paramAnnos.get(paramIndex).stream()
            .anyMatch(a -> a.className().stringValue().endsWith("/Nullable;"));
  }

  /** Bytecode-level detection of the two null-guard shapes on a given parameter slot. */
  private static final class GuardScanner {

    private static final ClassDesc OBJECTS = ClassDesc.of(Objects.class.getName());
    private static final ClassDesc API_USAGE_LOGGER = ClassDesc.of(ApiUsageLogger.class.getName());

    private GuardScanner() {}

    /** True iff {@code paramSlot} is passed as the first arg to {@code Objects.requireNonNull}. */
    static boolean hasRequireNonNullGuard(MethodModel method, int paramSlot) {
      List<Instruction> instructions =
          method.code().stream()
              .flatMap(code -> code.elementStream())
              .filter(Instruction.class::isInstance)
              .map(Instruction.class::cast)
              .toList();
      for (int i = 0; i < instructions.size(); i++) {
        if (!(instructions.get(i) instanceof InvokeInstruction invoke)
            || invoke.opcode() != Opcode.INVOKESTATIC
            || !invoke.owner().matches(OBJECTS)
            || !"requireNonNull".equals(invoke.name().stringValue())) {
          continue;
        }
        // Skip the trailing message argument for two-argument overloads.
        int argumentIndex = i - invoke.typeSymbol().parameterCount();
        if (argumentIndex >= 0 && isReferenceLoad(instructions.get(argumentIndex), paramSlot)) {
          return true;
        }
      }
      return false;
    }

    /**
     * True iff the method contains {@code if (paramSlot == null) { ... ApiUsageLogger.log*(...);
     * ...; return; }}: an {@code IFNONNULL} on the param whose fall-through branch contains a call
     * to {@code ApiUsageLogger} before the jump target.
     */
    static boolean hasApiUsageLoggerGuard(MethodModel method, int paramSlot) {
      List<CodeElement> elements =
          method.code().stream().flatMap(code -> code.elementStream()).toList();
      for (int i = 0; i < elements.size(); i++) {
        if (!isReferenceLoad(elements.get(i), paramSlot)) {
          continue;
        }
        int next = i + 1;
        while (next < elements.size() && !(elements.get(next) instanceof Instruction)) {
          next++;
        }
        if (next >= elements.size()
            || !(elements.get(next) instanceof BranchInstruction jump)
            || jump.opcode() != Opcode.IFNONNULL) {
          continue;
        }
        for (int cursor = next + 1; cursor < elements.size(); cursor++) {
          CodeElement element = elements.get(cursor);
          if (element instanceof LabelTarget target && target.label().equals(jump.target())) {
            break;
          }
          if (element instanceof InvokeInstruction invoke
              && invoke.owner().matches(API_USAGE_LOGGER)) {
            return true;
          }
        }
      }
      return false;
    }

    private static boolean isReferenceLoad(CodeElement element, int paramSlot) {
      return element instanceof LoadInstruction load
          && load.typeKind() == TypeKind.REFERENCE
          && load.slot() == paramSlot;
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
            .append(prettyParams(f.methodDesc))
            .append(" param #")
            .append(f.paramIndex)
            .append(" (")
            .append(prettyParamType(f.methodDesc, f.paramIndex))
            .append("): ")
            .append(f.message)
            .append('\n');
      }
    }
    return sb.toString();
  }

  private static String prettyParams(String methodDesc) {
    return MethodTypeDesc.ofDescriptor(methodDesc).parameterList().stream()
        .map(ClassDesc::displayName)
        .collect(Collectors.joining(", ", "(", ")"));
  }

  private static String prettyParamType(String methodDesc, int paramIndex) {
    MethodTypeDesc type = MethodTypeDesc.ofDescriptor(methodDesc);
    if (paramIndex < 0 || paramIndex >= type.parameterCount()) {
      return "?";
    }
    return type.parameterType(paramIndex).displayName();
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
