/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.sdk.extension.jfr;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.ContextStorageProvider;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;

public class JfrContextStorageProvider implements ContextStorageProvider {

    private static final ContextStorage INSTANCE = new JfrContextStorage(ContextStorage.get());

    @Override
    public ContextStorage get() {
        return INSTANCE;
    }

    static class JfrContextStorage implements ContextStorage {

        private final ContextStorage wrappedStorage;

        public JfrContextStorage(ContextStorage wrappedStorage) {
            this.wrappedStorage = wrappedStorage;
        }

        @Override
        public Scope attach(Context toAttach) {
            Scope scope = wrappedStorage.attach(toAttach);
            ScopeEvent event = new ScopeEvent(Span.fromContext(toAttach).getSpanContext());
            event.begin();
            return () -> {
                event.commit();
                scope.close();
            };
        }

        @Override
        public Context current() {
            return wrappedStorage.current();
        }
    }
}
