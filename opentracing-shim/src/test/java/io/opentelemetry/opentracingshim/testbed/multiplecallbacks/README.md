# Multiple callbacks example.

This example shows a `Span` created for a top-level operation, covering a set of asynchronous operations (representing callbacks), and have this `Span` finished when **all** of them have been executed.

`Client.send()` is used to create a new asynchronous operation (callback), and in turn every operation both restores the active `Span`, and creates a child `Span` (useful for measuring the performance of each callback). `AutoFinishScopeManager` is used so the related callbacks can be referenced, and properly finish the main `Span` when all pending work is done.

```java
// Client.send()
final Continuation cont = ((AutoFinishScopeManager)tracer.scopeManager()).captureScope();

return executor.submit(new Callable<Object>() {
    @Override
    public Object call() throws Exception {
	logger.info("Child thread with message '{}' started", message);

	try (Scope parentScope = cont.activate()) {

	    Span span = tracer.buildSpan("subtask").start();
	    try (Scope subtaskScope = tracer.activateSpan(span)) {
		...

```
