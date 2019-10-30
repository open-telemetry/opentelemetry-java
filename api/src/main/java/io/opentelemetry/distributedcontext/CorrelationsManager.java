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

package io.opentelemetry.distributedcontext;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.HttpExtractor;
import io.opentelemetry.context.propagation.HttpInjector;
import java.util.List;

public interface CorrelationsManager {
  public Context setValue(Context ctx, CorrelationLabel label, HopLimit hopLimit);

  public Context setValues(Context ctx, List<CorrelationLabel> labels, HopLimit hopLimit);

  public interface CorrelationLabel {
    public String key();

    public String value();
  }

  public enum HopLimit {
    NO_PROPAGATION,
    UNLIMITED_PROPAGATION
  }

  public HttpInjector getHttpInjector();

  public HttpExtractor getHttpExtractor();
}
