/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
@ParametersAreNonnullByDefault
package io.opentelemetry.extension.auto.annotations;

import javax.annotation.ParametersAreNonnullByDefault;
