/*
 * Copyright The OpenTelemetry Authors
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
package io.opentelemetry.context;

import java.util.concurrent.Executor;

/**
 * A Java SPI (Service Provider Interface) to allow replacing the default {@link ContextStorage}.
 * This can be useful if, for example, you want to store OpenTelemetry {@link Context} in another
 * context propagation system. For example, the returned {@link ContextStorage} could delegate to
 * methods in
 *
 * <p><a
 * href="https://javadoc.io/doc/com.linecorp.armeria/armeria-javadoc/latest/com/linecorp/armeria/common/RequestContext.html">{@code
 * com.linecorp.armeria.common.RequestContext}</a>, <a
 * href="https://grpc.github.io/grpc-java/javadoc/io/grpc/Context.html">{@code
 * io.grpc.context.Context}</a>, or <a
 * href="https://download.eclipse.org/microprofile/microprofile-context-propagation-1.0.2/apidocs/org/eclipse/microprofile/context/ThreadContext.html">{@code
 * org.eclipse.microprofile.context.ThreadContext}</a>
 *
 * <p>if you are already using one of those systems in your application. Then you would not have to
 * use methods like {@link Context#wrap(Executor)} and can use your current system instead.
 */
public interface ContextStorageProvider {

  /** Returns the {@link ContextStorage} to use to store {@link Context}. */
  ContextStorage get();
}
