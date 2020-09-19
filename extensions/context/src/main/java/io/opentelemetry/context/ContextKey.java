package io.opentelemetry.context;

/**
 * Key for indexing values of type {@link T} stored in a {@link Context}. {@link ContextKey} are
 * compared by reference, so it is generally expected that only one {@link ContextKey} is created
 * for a particular type of context value.
 *
 * <pre>{@code
 *
 * public class ContextUser {
 *
 *   private static final ContextKey<MyState> KEY = ContextKey.named("MyState");
 *
 *   public Context startWork() {
 *     return Context.withValue(KEY, new MyState());
 *   }
 *
 *   public void continueWork() {
 *     MyState state = Context.current().getValue(KEY);
 *     // Keys are compared by reference only.
 *     assert state != Context.current().getValue(ContextKey.named("MyState"));
 *     ...
 *   }
 * }
 *
 * }</pre>
 */
public interface ContextKey<T> {

  /**
   * Returns a new {@link DefaultContextKey} with the given debug name. The name does not impact
   * behavior and is only for debugging purposes. Multiple different keys with the same name will be
   * separate keys.
   */
  static <T> ContextKey<T> named(String name) {
    return new DefaultContextKey<>(name);
  }
}
