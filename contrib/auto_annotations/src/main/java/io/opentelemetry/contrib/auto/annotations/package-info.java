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

/**
 * This module contains various annotations that can be used by clients of OpenTelemetry API. They
 * don't provide any functionality by themselves, but other modules, e.g. <a
 * href="https://github.com/open-telemetry/opentelemetry-auto-instr-java">OpenTelemetry
 * Auto-Instrumentation</a> can use them to enhance their functionality.
 *
 * <p>Note: If you are a library developer, then you should NOT use this module, because it is
 * useless without some kind of annotation processing, such as bytecode manipulation during runtime.
 * You cannot guarantee that users of your library will use that in their production system.
 */
package io.opentelemetry.contrib.auto.annotations;
