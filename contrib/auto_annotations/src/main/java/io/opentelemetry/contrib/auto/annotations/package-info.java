/**
 * This module contains various annotations that can be used by clients of OpenTelemetry API. They
 * don't provide any functionality by themselves, but other modules, e.g. <a
 * href="https://github.com/open-telemetry/opentelemetry-auto-instr-java">OpenTelemetry
 * Auto-Instrumentation</a> can use them to enhance their work.
 *
 * <p>NB! If you are a library developer, then probably you should NOT use this module. Because it
 * is useless without some kind of byte code manipulation during runtime. And you cannot guarantee
 * that users of your library will use that in their production system.
 */
package io.opentelemetry.contrib.auto.annotations;
