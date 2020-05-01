/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib.auto.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks that an execution of this method or constructor should result in a new
 * {@link io.opentelemetry.trace.Span}.
 *
 * <p>Application developers can use this annotation to signal OpenTelemetry auto-instrumentation
 * that a new span should be created whenever marked method is executed.
 *
 * <p>If you are a library developer, then probably you should NOT use this annotation, because it
 * is non-functional without the OpenTelemetry auto-instrumentation agent, or some other annotation
 * processor.
 *
 * @see <a href="https://github.com/open-telemetry/opentelemetry-auto-instr-java">OpenTelemetry
 *     Auto-Instrumentation</a>
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithSpan {
  /**
   * Optional name of the created span.
   *
   * <p>If not specified, an appropriate default name should be created by auto-instrumentation.
   * E.g. {@code "className"."method"}
   */
  String value() default "";
}
