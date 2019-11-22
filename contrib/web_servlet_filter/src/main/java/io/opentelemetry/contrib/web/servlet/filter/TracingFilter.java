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

package io.opentelemetry.contrib.web.servlet.filter;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ChainedPropagators;
import io.opentelemetry.context.propagation.DefaultHttpInjector;
import io.opentelemetry.context.propagation.Propagators;
import io.opentelemetry.distributedcontext.propagation.DefaultCorrelationContextExtractor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.propagation.ContextKeys;
import io.opentelemetry.trace.propagation.HttpTraceContextExtractor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tracing servlet filter.
 *
 * <p>Filter can be programmatically added to {@link ServletContext} or initialized via web.xml.
 *
 * <p>Following code examples show possible initialization:
 *
 * <pre>{@code
 * TracingFilter filter = new TracingFilter(tracer);
 *  servletContext.addFilter("tracingFilter", filter);
 * }</pre>
 *
 * <p>Or include filter in web.xml and:
 *
 * <pre>{@code
 * GlobalTracer.register(tracer);
 * // optional, if no present ServletFilterSpanDecorator.STANDARD_TAGS is applied
 * servletContext.setAttribute({@link TracingFilter#SPAN_DECORATORS}, listOfDecorators);  *
 * }</pre>
 *
 * <p>Current server span context is accessible via {@link HttpServletRequest#getAttribute(String)}
 * with name {@link TracingFilter#SERVER_SPAN_CONTEXT}.
 *
 * @author Pavol Loffay
 */
public class TracingFilter implements Filter {
  private static final Logger log = Logger.getLogger(TracingFilter.class.getName());

  /** Use as a key of {@link ServletContext#setAttribute(String, Object)} to set span decorators. */
  public static final String SPAN_DECORATORS = TracingFilter.class.getName() + ".spanDecorators";
  /** Use as a key of {@link ServletContext#setAttribute(String, Object)} to skip pattern. */
  public static final String SKIP_PATTERN = TracingFilter.class.getName() + ".skipPattern";

  /**
   * Used as a key of {@link HttpServletRequest#setAttribute(String, Object)} to inject server span
   * context.
   */
  public static final String SERVER_SPAN_CONTEXT =
      TracingFilter.class.getName() + ".activeSpanContext";

  protected Tracer tracer;
  private List<ServletFilterSpanDecorator> spanDecorators;
  private Pattern skipPattern;

  /**
   * Summary.
   *
   * @param tracer the tracer.
   */
  public TracingFilter(Tracer tracer) {
    this(tracer, Collections.singletonList(ServletFilterSpanDecorator.STANDARD_TAGS), null);
  }

  /**
   * Summary.
   *
   * @param tracer tracer
   * @param spanDecorators decorators
   * @param skipPattern null or pattern to exclude certain paths from tracing e.g. "/health"
   */
  public TracingFilter(
      Tracer tracer, List<ServletFilterSpanDecorator> spanDecorators, Pattern skipPattern) {
    this.tracer = tracer;
    this.spanDecorators = new ArrayList<>(spanDecorators);
    this.spanDecorators.removeAll(Collections.singleton(null));
    this.skipPattern = skipPattern;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    ServletContext servletContext = filterConfig.getServletContext();

    // Check whether the servlet context provides a tracer
    Object tracerObj = servletContext.getAttribute(Tracer.class.getName());
    if (tracerObj instanceof Tracer) {
      tracer = (Tracer) tracerObj;
    } else {
      // Add current tracer to servlet context, so available to webapp
      servletContext.setAttribute(Tracer.class.getName(), tracer);
    }

    // use decorators from context attributes
    Object contextAttribute = servletContext.getAttribute(SPAN_DECORATORS);
    if (contextAttribute instanceof Collection) {
      List<ServletFilterSpanDecorator> decorators = new ArrayList<>();
      for (Object decorator : (Collection) contextAttribute) {
        if (decorator instanceof ServletFilterSpanDecorator) {
          decorators.add((ServletFilterSpanDecorator) decorator);
        } else {
          log.severe(decorator + " is not an instance of " + ServletFilterSpanDecorator.class);
        }
      }
      this.spanDecorators = decorators.size() > 0 ? decorators : this.spanDecorators;
    }

    contextAttribute = servletContext.getAttribute(SKIP_PATTERN);
    if (contextAttribute instanceof Pattern) {
      skipPattern = (Pattern) contextAttribute;
    }

    // Initialize the propagators.
    Propagators propagators =
        Propagators.create(
            new DefaultHttpInjector(),
            ChainedPropagators.chain(
                new HttpTraceContextExtractor(), new DefaultCorrelationContextExtractor()));
    OpenTelemetry.setPropagators(propagators);
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

    if (!isTraced(httpRequest, httpResponse)) {
      chain.doFilter(httpRequest, httpResponse);
      return;
    }

    /** If request is traced then do not start new span. */
    if (servletRequest.getAttribute(SERVER_SPAN_CONTEXT) != null) {
      chain.doFilter(servletRequest, servletResponse);
    } else {
      /**
       * SpanContext *and* other members (such as correlationcontext) would be extracted here, and
       * make it available in the returned Context object.
       *
       * <p>For further consumption, the returned Context object would need to be explicitly passed
       * to DistributedContext/Baggage handlers, or else set it automatically as the current
       * instance.
       */
      Context ctx =
          OpenTelemetry.getPropagators()
              .getHttpExtractor()
              .extract(Context.current(), httpRequest, HttpServletRequestGetter.getInstance());
      SpanContext extractedContext = ctx.getValue(ContextKeys.getSpanContextKey());

      final Span span =
          tracer
              .spanBuilder(httpRequest.getMethod())
              .setParent(extractedContext)
              .setSpanKind(Span.Kind.SERVER)
              .startSpan();

      httpRequest.setAttribute(SERVER_SPAN_CONTEXT, span.getContext());

      for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
        spanDecorator.onRequest(httpRequest, span);
      }

      Scope scope = tracer.withSpan(span);
      try {
        chain.doFilter(servletRequest, servletResponse);
        if (!httpRequest.isAsyncStarted()) {
          for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
            spanDecorator.onResponse(httpRequest, httpResponse, span);
          }
        }
        // catch all exceptions (e.g. RuntimeException, ServletException...)
      } catch (Throwable ex) {
        for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
          spanDecorator.onError(httpRequest, httpResponse, ex, span);
        }
        throw ex;
      } finally {
        scope.close();

        if (httpRequest.isAsyncStarted()) {
          // what if async is already finished? This would not be called
          httpRequest
              .getAsyncContext()
              .addListener(
                  new AsyncListener() {
                    @Override
                    public void onComplete(AsyncEvent event) throws IOException {
                      HttpServletRequest httpRequest =
                          (HttpServletRequest) event.getSuppliedRequest();
                      HttpServletResponse httpResponse =
                          (HttpServletResponse) event.getSuppliedResponse();
                      for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                        spanDecorator.onResponse(httpRequest, httpResponse, span);
                      }
                      span.end();
                    }

                    @Override
                    public void onTimeout(AsyncEvent event) throws IOException {
                      HttpServletRequest httpRequest =
                          (HttpServletRequest) event.getSuppliedRequest();
                      HttpServletResponse httpResponse =
                          (HttpServletResponse) event.getSuppliedResponse();
                      for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                        spanDecorator.onTimeout(
                            httpRequest, httpResponse, event.getAsyncContext().getTimeout(), span);
                      }
                    }

                    @Override
                    public void onError(AsyncEvent event) throws IOException {
                      HttpServletRequest httpRequest =
                          (HttpServletRequest) event.getSuppliedRequest();
                      HttpServletResponse httpResponse =
                          (HttpServletResponse) event.getSuppliedResponse();
                      for (ServletFilterSpanDecorator spanDecorator : spanDecorators) {
                        spanDecorator.onError(
                            httpRequest, httpResponse, event.getThrowable(), span);
                      }
                    }

                    @Override
                    public void onStartAsync(AsyncEvent event) throws IOException {}
                  });
        } else {
          // If not async, then need to explicitly finish the span associated with the scope.
          // This is necessary, as we don't know whether this request is being handled
          // asynchronously until after the scope has already been started.
          span.end();
        }
      }
    }
  }

  @Override
  public void destroy() {}

  /**
   * It checks whether a request should be traced or not.
   *
   * @param httpServletRequest request
   * @param httpServletResponse response
   * @return whether request should be traced or not
   */
  protected boolean isTraced(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    // skip URLs matching skip pattern
    // e.g. pattern is defined as '/health|/status' then URL 'http://localhost:5000/context/health'
    // won't be traced
    if (skipPattern != null) {
      String url =
          httpServletRequest
              .getRequestURI()
              .substring(httpServletRequest.getContextPath().length());
      return !skipPattern.matcher(url).matches();
    }

    return true;
  }

  /**
   * Get context of server span.
   *
   * @param servletRequest request
   * @return server span context
   */
  public static SpanContext serverSpanContext(ServletRequest servletRequest) {
    return (SpanContext) servletRequest.getAttribute(SERVER_SPAN_CONTEXT);
  }
}
