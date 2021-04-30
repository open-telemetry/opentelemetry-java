/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks that a parameter of a method annotated by the {@link WithSpan} annotation
 * should be added as an attribute to the newly created {@link io.opentelemetry.api.trace.Span}.
 * Using this annotation is equivalent to calling {@code Span.currentSpan().setAttribute(...)}
 * within the body of the method.
 *
 * <p>Application developers can use this annotation to signal OpenTelemetry auto-instrumentation
 * that a new span attribute should be added to a span created when the parent method is executed.
 *
 * <p>If you are a library developer, then probably you should NOT use this annotation, because it
 * is non-functional without the OpenTelemetry auto-instrumentation agent, or some other annotation
 * processor.
 *
 * @see <a href="https://github.com/open-telemetry/opentelemetry-java-instrumentation">OpenTelemetry
 *     OpenTelemetry Instrumentation for Java</a>
 * @since 1.2.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpanAttribute {
  /**
   * Optional name of the attribute.
   *
   * <p>If not specified and the code is compiled using the `{@code -parameters}` argument to
   * `javac`, the parameter name will be used instead. If the parameter name is not available, e.g.,
   * because the code was not compiled with that flag, the attribute will be ignored.
   */
  String value() default "";
}
