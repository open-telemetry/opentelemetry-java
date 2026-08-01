# ScopedValue-backed Context Storage (Java 25+)

This document describes the design of `ScopedValueContextStorage` and `ScopedValueContext`, an
alternative context implementation that activates automatically on Java 25+ when no other
`ContextStorageProvider` SPI is registered.

## Motivation

The existing `ThreadLocalContextStorage` / `ArrayBasedContext` pair works correctly for platform
threads, but `ThreadLocal` has two drawbacks in a virtual-thread-heavy environment:

- **No automatic inheritance.** When a virtual thread is spawned, it starts with an empty
  `ThreadLocal` map. Propagating context to child threads requires explicit wrapping via
  `Context.current().wrap(runnable)` — and only if the wrapping happens at the call site. If it
  is forgotten, context is silently lost.
- **Per-thread memory cost.** Each virtual thread that touches a `ThreadLocal` allocates its own
  storage slot. With millions of virtual threads this becomes significant.

`ScopedValue` (finalized in Java 21, refined in Java 25) addresses both: bindings are immutable,
inherited by virtual thread children automatically, and stored in a compact structure shared across
the inheritance chain.

## Data model

`ScopedValueContext` mirrors `ArrayBasedContext` exactly in its internal representation: a flat
`Object[]` array of alternating keys and values — `[key0, val0, key1, val1, ...]`. `get(key)` is
a linear identity scan; `with(key, value)` is copy-on-write. No change to the lookup algorithm.

The only structural difference is how that array is made "current". Instead of storing a
`Context` object in a `ThreadLocal<Context>`, a single `ScopedValue<ScopedValueContext>` named
`SCOPED` holds a reference to the active `ScopedValueContext`. When `wrap()` is called, the array
is bound via `ScopedValue.where(SCOPED, this).run(body)` for the duration of `body`.

`ContextStorage.root()` is overridden to return `ScopedValueContext.ROOT` (an instance with an
empty array), which seeds the `with()` chain so `ArrayBasedContext` instances are never created.

## Storage model: two paths

`ScopedValueContextStorage` maintains two storage paths side by side:

| Path | Mechanism | Established by |
|------|-----------|----------------|
| ScopedValue | `SCOPED` binding | `wrap(Runnable)` and all other `wrap*` overrides |
| ThreadLocal | `THREAD_LOCAL` binding | `makeCurrent()` → `attach()` |

`current()` checks `THREAD_LOCAL` first, then falls back to `SCOPED`:

```java
public Context current() {
    Context local = THREAD_LOCAL.get();
    if (local != null) return local;
    return SCOPED.isBound() ? SCOPED.get() : null;
}
```

This ordering ensures that an explicit `makeCurrent()` call inside a `wrap()` scope correctly
shadows the inherited ScopedValue binding for the current thread, while leaving SCOPED intact for
any virtual thread children spawned concurrently.

## Where ThreadLocal is still involved

### Case 1: Direct `makeCurrent()` call

```java
try (Scope scope = context.makeCurrent()) {
    // ScopedValueContextStorage.attach() → THREAD_LOCAL = context
    // SCOPED is never bound
    // Virtual threads spawned here see no context
}
// ScopeImpl.close() → THREAD_LOCAL.remove()
```

`makeCurrent()` is not overridden in `ScopedValueContext` and cannot be. `Scope makeCurrent()`
returns a closeable whose `close()` is called later at an arbitrary point — that open-scope model
has no equivalent in `ScopedValue`, which requires the body to be provided upfront via
`ScopedValue.where(...).run(body)`. There is no way to imperatively "unbind" a ScopedValue.

As a result, code written in the traditional try-with-resources style gets pure ThreadLocal
semantics — functionally identical to `ThreadLocalContextStorage`. No virtual thread propagation.

### Case 2: `wrap()`-based usage

```java
context.wrap(() -> {
    // runInScope: THREAD_LOCAL cleared, SCOPED = context
    // Context.current() reads from SCOPED ✓
    // Virtual threads spawned here inherit SCOPED automatically ✓

    anotherCtx.makeCurrent(); // THREAD_LOCAL = anotherCtx, shadows SCOPED
    // scope.close(): threadLocalBefore was null → THREAD_LOCAL.remove()
    // current() falls back to SCOPED = context again ✓
}).run();
```

`wrap()` (and `wrapFunction`, `wrapConsumer`, `wrapSupplier`, `wrap(Callable)`) are all overridden
in `ScopedValueContext`. Each override calls the private `runInScope` / `callInScope` helpers,
which:

1. Save and remove any active `THREAD_LOCAL` override so `current()` sees `SCOPED`.
2. Bind `SCOPED` to `this` for the duration of the body via `ScopedValue.where(SCOPED, this).run(body)`.
3. Restore `THREAD_LOCAL` on exit.

ThreadLocal only appears in this path if something inside the body explicitly calls `makeCurrent()`,
and in that case it is correctly scoped and cleaned up when the inner `Scope` closes.

`wrap(Executor)`, `wrap(ExecutorService)`, and `wrap(ScheduledExecutorService)` are **not**
overridden — their default implementations delegate to `wrap(Runnable)` and `wrap(Callable)`, so
they benefit from the ScopedValue path automatically via polymorphism.

### Case 3: Mixed — `makeCurrent()` outer, `wrap()` inner

```java
try (Scope scope = context.makeCurrent()) {
    // THREAD_LOCAL = context, SCOPED not bound

    context.with(key, val).wrap(runnable).run();
    // runInScope: saves THREAD_LOCAL=context, removes it, binds SCOPED=newCtx
    // Inside runnable: current() reads SCOPED = newCtx ✓
    // Virtual threads spawned here inherit SCOPED = newCtx ✓
    // On exit: THREAD_LOCAL restored to context ✓
}
```

The two paths compose correctly. `wrap()` suspends the ThreadLocal override for the duration of the
body and restores it on the way out, so the outer `makeCurrent()` scope is unaffected.

## Summary

ThreadLocal is required in exactly one situation: when `makeCurrent()` is called — either directly
by user code, or transitively. All transitive calls have been eliminated by overriding the full set
of `wrap*` methods on `ScopedValueContext`. Code that exclusively uses the `wrap`-based APIs gets
full ScopedValue semantics: automatic virtual thread propagation and no per-thread allocation.
Code that uses `makeCurrent()` continues to work correctly but does not gain the virtual thread
propagation benefit.
