/*
 * Copyright 2019, OpenTelemetry Authors
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

/**
 * API for associating entries with scoped operations.
 *
 * <p>This package manages a set of entries in the {@code io.grpc.Context}. The entries can be used
 * to label anything that is associated with a specific operation. For example, the {@code
 * opentelemetry.stats} package labels all stats with the current entries.
 *
 * <p>{@link io.opentelemetry.correlationcontext.Entry Entrys} are key-value pairs. The {@link
 * io.opentelemetry.correlationcontext.EntryKey keys} and {@link
 * io.opentelemetry.correlationcontext.EntryValue values} are wrapped {@code String}s. They are
 * stored as a map in a {@link io.opentelemetry.correlationcontext.CorrelationContext}.
 *
 * <p>Note that entries are independent of the tracing data that is propagated in the {@code
 * io.grpc.Context}, such as trace ID.
 */
// TODO: Add code examples.
package io.opentelemetry.correlationcontext;
