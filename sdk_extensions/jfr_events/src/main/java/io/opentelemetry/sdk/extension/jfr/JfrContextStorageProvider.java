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
package io.opentelemetry.sdk.extension.jfr;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.ContextStorageProvider;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;

public class JfrContextStorageProvider implements ContextStorageProvider {

    @Override
    public ContextStorage get() {
        ContextStorage parentStorage = ContextStorage.get();
        return new ContextStorage() {
            @Override
            public Scope attach(Context toAttach) {
                Scope scope = parentStorage.attach(toAttach);
                ScopeEvent event = new ScopeEvent(Span.fromContext(toAttach).getSpanContext());
                event.begin();
                return () -> {
                    event.commit();
                    scope.close();
                };
            }

            @Override
            public Context current() {
                return parentStorage.current();
            }
        };
    }
}
