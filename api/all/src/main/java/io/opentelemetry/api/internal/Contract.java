/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:

/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.api.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies some aspects of the method behavior depending on the arguments. Can be used by tools
 * for advanced data flow analysis. Note that this annotation just describes how the code works and
 * doesn't add any functionality by means of code generation.
 *
 * <p>Method contract has the following syntax:<br>
 *
 * <pre>{@code
 * contract ::= (clause ';')* clause
 * clause ::= args '->' effect
 * args ::= ((arg ',')* arg )?
 * arg ::= value-constraint
 * value-constraint ::= '_' | 'null' | '!null' | 'false' | 'true'
 * effect ::= value-constraint | 'fail' | 'this' | 'new' | 'param<N>'
 * }</pre>
 *
 * <p>The constraints denote the following:<br>
 *
 * <ul>
 *   <li>_ - any value
 *   <li>null - null value
 *   <li>!null - a value statically proved to be not-null
 *   <li>true - true boolean value
 *   <li>false - false boolean value
 * </ul>
 *
 * <p>The additional return values denote the following:<br>
 *
 * <ul>
 *   <li>fail - the method throws an exception, if the arguments satisfy argument constraints
 *   <li>new - (supported in IntelliJ IDEA since version 2018.2) the method returns a non-null new
 *       object which is distinct from any other object existing in the heap prior to method
 *       execution. If method is also pure, then we can be sure that the new object is not stored to
 *       any field/array and will be lost if method return value is not used.
 *   <li>this - (supported in IntelliJ IDEA since version 2018.2) the method returns its qualifier
 *       value (not applicable for static methods)
 *   <li>param1, param2, ... - (supported in IntelliJ IDEA since version 2018.2) the method returns
 *       its first (second, ...) parameter value
 * </ul>
 *
 * <p>Examples:
 *
 * <p>{@code @Contract("_, null -> null")} - the method returns null if its second argument is null
 * <br>
 * {@code @Contract("_, null -> null; _, !null -> !null")} - the method returns null if its second
 * argument is null and not-null otherwise<br>
 * {@code @Contract("true -> fail")} - a typical {@code assertFalse} method which throws an
 * exception if {@code true} is passed to it<br>
 * {@code @Contract("_ -> this")} - the method always returns its qualifier (e.g. {@link
 * StringBuilder#append(String)}). {@code @Contract("null -> fail; _ -> param1")} - the method
 * throws an exception if the first argument is null, otherwise it returns the first argument (e.g.
 * {@code Objects.requireNonNull}).<br>
 * {@code @Contract("!null, _ -> param1; null, !null -> param2; null, null -> fail")} - the method
 * returns the first non-null argument, or throws an exception if both arguments are null (e.g.
 * {@code Objects.requireNonNullElse} in Java 9).<br>
 *
 * <p>This annotation is the same provided with Jetbrains annotations and used by Nullaway for
 * verifying nullness. We copy the annotation to avoid an external dependency.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Contract {
  /**
   * Contains the contract clauses describing causal relations between call arguments and the
   * returned value.
   */
  String value() default "";
}
