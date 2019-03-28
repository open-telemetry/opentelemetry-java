/*
 * Copyright 2019, OpenConsensus Authors
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
 * API for associating tags with scoped operations.
 *
 * <p>This package manages a set of tags in the {@code io.grpc.Context}. The tags can be used to
 * label anything that is associated with a specific operation. For example, the {@code
 * io.opencensus.stats} package labels all stats with the current tags.
 *
 * <p>{@link openconsensus.tags.Tag Tags} are key-value pairs. The {@link openconsensus.tags.TagKey
 * keys} and {@link openconsensus.tags.TagValue values} are wrapped {@code String}s. They are stored
 * as a map in a {@link openconsensus.tags.TagMap}.
 *
 * <p>Note that tags are independent of the tracing data that is propagated in the {@code
 * io.grpc.Context}, such as trace ID.
 */
// TODO(sebright): Add code examples.
package openconsensus.tags;
