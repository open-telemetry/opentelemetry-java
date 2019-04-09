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
 * This package describes the Metrics data model. Metrics are a data model for what stats exporters
 * take as input. This data model may eventually become the wire format for metrics.
 *
 * <p>WARNING: Currently all the public classes under this package are marked as {@link
 * openconsensus.common.ExperimentalApi}. The classes and APIs under {@link openconsensus.metrics}
 * are likely to get backwards-incompatible updates in the future. DO NOT USE except for
 * experimental purposes.
 */
@ExperimentalApi
package openconsensus.metrics;

import openconsensus.common.ExperimentalApi;
