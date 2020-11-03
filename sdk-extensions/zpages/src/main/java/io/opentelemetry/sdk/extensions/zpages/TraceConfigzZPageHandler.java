/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.zpages;

import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TraceConfigzZPageHandler extends ZPageHandler {
  private static final String TRACE_CONFIGZ_URL = "/traceconfigz";
  private static final String TRACE_CONFIGZ_NAME = "TraceConfigZ";
  private static final String TRACE_CONFIGZ_DESCRIPTION =
      "TraceConfigZ displays information about the current active tracing configuration"
          + " and allows users to change it";
  private static final String QUERY_STRING_ACTION = "action";
  private static final String QUERY_STRING_ACTION_CHANGE = "change";
  private static final String QUERY_STRING_ACTION_DEFAULT = "default";
  private static final String QUERY_STRING_SAMPLING_PROBABILITY = "samplingprobability";
  private static final String QUERY_STRING_MAX_NUM_OF_ATTRIBUTES = "maxnumofattributes";
  private static final String QUERY_STRING_MAX_NUM_OF_EVENTS = "maxnumofevents";
  private static final String QUERY_STRING_MAX_NUM_OF_LINKS = "maxnumoflinks";
  private static final String QUERY_STRING_MAX_NUM_OF_ATTRIBUTES_PER_EVENT =
      "maxnumofattributesperevent";
  private static final String QUERY_STRING_MAX_NUM_OF_ATTRIBUTES_PER_LINK =
      "maxnumofattributesperlink";
  // Background color used for zebra striping rows in table
  private static final String ZEBRA_STRIPE_COLOR = "#e6e6e6";
  private static final Logger logger = Logger.getLogger(TraceConfigzZPageHandler.class.getName());
  private final TracerSdkManagement tracerSdkManagement;

  TraceConfigzZPageHandler(TracerSdkManagement tracerSdkManagement) {
    this.tracerSdkManagement = tracerSdkManagement;
  }

  @Override
  public String getUrlPath() {
    return TRACE_CONFIGZ_URL;
  }

  @Override
  public String getPageName() {
    return TRACE_CONFIGZ_NAME;
  }

  @Override
  public String getPageDescription() {
    return TRACE_CONFIGZ_DESCRIPTION;
  }

  /**
   * Emits CSS styles to the {@link PrintStream} {@code out}. Content emited by this function should
   * be enclosed by <head></head> tag.
   *
   * @param out the {@link PrintStream} {@code out}.
   */
  private static void emitHtmlStyle(PrintStream out) {
    out.print("<style>");
    out.print(ZPageStyle.style);
    out.print("</style>");
  }

  /**
   * Emits a row of the change tracing parameter table to the {@link PrintStream} {@code out}. Each
   * row corresponds to one tracing parameter.
   *
   * @param out the {@link PrintStream} {@code out}.
   * @param rowName the display name of the corresponding tracing parameter.
   * @param paramName the name of the corresponding tracing parameter (this will be used to
   *     construct the query parameter in URL).
   * @param inputPlaceHolder placeholder for the <input> HTML element.
   * @param paramDefaultValue the default value of the corresponding tracing parameter.
   * @param zebraStripeColor hex code of the color used for zebra striping rows.
   * @param zebraStripe boolean indicating if the row is zebra striped.
   */
  private static void emitChangeTableRow(
      PrintStream out,
      String rowName,
      String paramName,
      String inputPlaceHolder,
      String paramDefaultValue,
      String zebraStripeColor,
      boolean zebraStripe) {
    if (zebraStripe) {
      out.print("<tr style=\"background-color: " + zebraStripeColor + ";\">");
    } else {
      out.print("<tr>");
    }
    out.print("<td>Update " + rowName + "</td>");
    out.print(
        "<td class=\"border-left-dark\"><input type=text size=15 name="
            + paramName
            + " value=\"\" placeholder=\""
            + inputPlaceHolder
            + "\" /></td>");
    out.print("<td class=\"border-left-dark\">(" + paramDefaultValue + ")</td>");
    out.print("</tr>");
  }

  /**
   * Emits the change tracing parameter table to the {@link PrintStream} {@code out}.
   *
   * @param out the {@link PrintStream} {@code out}.
   */
  private static void emitChangeTable(PrintStream out) {
    out.print("<table style=\"border-spacing: 0; border: 1px solid #363636;\">");
    out.print("<tr class=\"bg-color\">");
    out.print(
        "<th colspan=2 style=\"text-align: left;\" class=\"header-text\">"
            + "<b>Update active TraceConfig</b></th>");
    out.print("<th colspan=1 class=\"header-text border-left-white\"><b>Default</b></th>");
    emitChangeTableRow(
        /* out= */ out,
        /* rowName= */ "SamplingProbability to",
        /* paramName= */ QUERY_STRING_SAMPLING_PROBABILITY,
        /* inputPlaceHolder= */ "[0.0, 1.0]",
        /* paramDefaultValue= */ TraceConfig.getDefault().getSampler().getDescription(),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ false);
    emitChangeTableRow(
        /* out= */ out,
        /* rowName= */ "MaxNumberOfAttributes to",
        /* paramName= */ QUERY_STRING_MAX_NUM_OF_ATTRIBUTES,
        /* inputPlaceHolder= */ "",
        /* paramDefaultValue= */ Integer.toString(
            TraceConfig.getDefault().getMaxNumberOfAttributes()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ true);
    emitChangeTableRow(
        /* out= */ out,
        /* rowName= */ "MaxNumberOfEvents to",
        /* paramName= */ QUERY_STRING_MAX_NUM_OF_EVENTS,
        /* inputPlaceHolder= */ "",
        /* paramDefaultValue= */ Integer.toString(TraceConfig.getDefault().getMaxNumberOfEvents()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ false);
    emitChangeTableRow(
        /* out= */ out,
        /* rowName= */ "MaxNumberOfLinks to",
        /* paramName= */ QUERY_STRING_MAX_NUM_OF_LINKS,
        /* inputPlaceHolder= */ "",
        /* paramDefaultValue= */ Integer.toString(TraceConfig.getDefault().getMaxNumberOfLinks()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ true);
    emitChangeTableRow(
        /* out= */ out,
        /* rowName= */ "MaxNumberOfAttributesPerEvent to",
        /* paramName= */ QUERY_STRING_MAX_NUM_OF_ATTRIBUTES_PER_EVENT,
        /* inputPlaceHolder= */ "",
        /* paramDefaultValue= */ Integer.toString(
            TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ false);
    emitChangeTableRow(
        /* out= */ out,
        /* rowName= */ "MaxNumberOfAttributesPerLink to",
        /* paramName= */ QUERY_STRING_MAX_NUM_OF_ATTRIBUTES_PER_LINK,
        /* inputPlaceHolder= */ "",
        /* paramDefaultValue= */ Integer.toString(
            TraceConfig.getDefault().getMaxNumberOfAttributesPerLink()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ true);
    out.print("</table>");
  }

  /**
   * Emits a row of the active tracing parameter table to the {@link PrintStream} {@code out}. Each
   * row corresponds to one tracing parameter.
   *
   * @param out the {@link PrintStream} {@code out}.
   * @param paramName the name of the corresponding tracing parameter.
   * @param paramValue the value of the corresponding tracing parameter.
   * @param zebraStripeColor hex code of the color used for zebra striping rows.
   * @param zebraStripe boolean indicating if the row is zebra striped.
   */
  private static void emitActiveTableRow(
      PrintStream out,
      String paramName,
      String paramValue,
      String zebraStripeColor,
      boolean zebraStripe) {
    if (zebraStripe) {
      out.print("<tr style=\"background-color: " + zebraStripeColor + ";\">");
    } else {
      out.print("<tr>");
    }
    out.print("<td>" + paramName + "</td>");
    out.print("<td class=\"border-left-dark\">" + paramValue + "</td>");
    out.print("</tr>");
  }

  /**
   * Emits the active tracing parameters table to the {@link PrintStream} {@code out}.
   *
   * @param out the {@link PrintStream} {@code out}.
   */
  private void emitActiveTable(PrintStream out) {
    out.print("<table style=\"border-spacing: 0; border: 1px solid #363636;\">");
    out.print("<tr class=\"bg-color\">");
    out.print("<th class=\"header-text\"><b>Name</b></th>");
    out.print("<th class=\"header-text border-left-white\"><b>Value</b></th>");
    out.print("</tr>");
    emitActiveTableRow(
        /* out= */ out,
        /* paramName= */ "Sampler",
        /* paramValue=*/ this.tracerSdkManagement
            .getActiveTraceConfig()
            .getSampler()
            .getDescription(),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ false);
    emitActiveTableRow(
        /* out= */ out,
        /* paramName= */ "MaxNumOfAttributes",
        /* paramValue=*/ Integer.toString(
            this.tracerSdkManagement.getActiveTraceConfig().getMaxNumberOfAttributes()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ true);
    emitActiveTableRow(
        /* out= */ out,
        /* paramName= */ "MaxNumOfEvents",
        /* paramValue=*/ Integer.toString(
            this.tracerSdkManagement.getActiveTraceConfig().getMaxNumberOfEvents()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ false);
    emitActiveTableRow(
        /* out= */ out,
        /* paramName= */ "MaxNumOfLinks",
        /* paramValue=*/ Integer.toString(
            this.tracerSdkManagement.getActiveTraceConfig().getMaxNumberOfLinks()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ true);
    emitActiveTableRow(
        /* out= */ out,
        /* paramName= */ "MaxNumOfAttributesPerEvent",
        /* paramValue=*/ Integer.toString(
            this.tracerSdkManagement.getActiveTraceConfig().getMaxNumberOfAttributesPerEvent()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe= */ false);
    emitActiveTableRow(
        /* out= */ out,
        /* paramName= */ "MaxNumOfAttributesPerLink",
        /* paramValue=*/ Integer.toString(
            this.tracerSdkManagement.getActiveTraceConfig().getMaxNumberOfAttributesPerLink()),
        /* zebraStripeColor= */ ZEBRA_STRIPE_COLOR,
        /* zebraStripe=*/ true);
    out.print("</table>");
  }

  /**
   * Emits HTML body content to the {@link PrintStream} {@code out}. Content emitted by this
   * function should be enclosed by <body></body> tag.
   *
   * @param out the {@link PrintStream} {@code out}.
   */
  private void emitHtmlBody(PrintStream out) {
    out.print(
        "<a href=\"/\"><img style=\"height: 90px;\" src=\"data:image/png;base64,"
            + ZPageLogo.getLogoBase64()
            + "\" /></a>");
    out.print("<h1>Trace Configuration</h1>");
    out.print("<form class=\"form-flex\" action=\"" + TRACE_CONFIGZ_URL + "\" method=\"post\">");
    out.print(
        "<input type=\"hidden\" name=\"action\" value=\"" + QUERY_STRING_ACTION_CHANGE + "\" />");
    emitChangeTable(out);
    // Button for submit
    out.print("<button class=\"button\" type=\"submit\" value=\"Submit\">Submit</button>");
    out.print("</form>");
    // Button for restore default
    out.print("<form class=\"form-flex\" action=\"" + TRACE_CONFIGZ_URL + "\" method=\"post\">");
    out.print(
        "<input type=\"hidden\" name=\"action\" value=\"" + QUERY_STRING_ACTION_DEFAULT + "\" />");
    out.print("<button class=\"button\" type=\"submit\" value=\"Submit\">Restore Default</button>");
    out.print("</form>");
    out.print("<h2>Active Tracing Parameters</h2>");
    emitActiveTable(out);
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    // PrintStream for emiting HTML contents
    try (PrintStream out = new PrintStream(outputStream, /* autoFlush= */ false, "UTF-8")) {
      out.print("<!DOCTYPE html>");
      out.print("<html lang=\"en\">");
      out.print("<head>");
      out.print("<meta charset=\"UTF-8\">");
      out.print(
          "<link rel=\"shortcut icon\" href=\"data:image/png;base64,"
              + ZPageLogo.getFaviconBase64()
              + "\" type=\"image/png\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
              + "rel=\"stylesheet\">");
      out.print(
          "<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">");
      out.print("<title>" + TRACE_CONFIGZ_NAME + "</title>");
      emitHtmlStyle(out);
      out.print("</head>");
      out.print("<body>");
      try {
        emitHtmlBody(out);
      } catch (Throwable t) {
        out.print("Error while generating HTML: " + t.toString());
        logger.log(Level.WARNING, "error while generating HTML", t);
      }
      out.print("</body>");
      out.print("</html>");
    } catch (Throwable t) {
      logger.log(Level.WARNING, "error while generating HTML", t);
    }
  }

  @Override
  public boolean processRequest(
      String requestMethod, Map<String, String> queryMap, OutputStream outputStream) {
    if (requestMethod.equalsIgnoreCase("POST")) {
      try {
        applyTraceConfig(queryMap);
      } catch (Throwable t) {
        try (PrintStream out = new PrintStream(outputStream, /* autoFlush= */ false, "UTF-8")) {
          out.print("<!DOCTYPE html>");
          out.print("<html lang=\"en\">");
          out.print("<head>");
          out.print("<meta charset=\"UTF-8\">");
          out.print(
              "<link rel=\"shortcut icon\" href=\"data:image/png;base64,"
                  + ZPageLogo.getFaviconBase64()
                  + "\" type=\"image/png\">");
          out.print(
              "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
                  + "rel=\"stylesheet\">");
          out.print(
              "<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">");
          out.print("<title>" + TRACE_CONFIGZ_NAME + "</title>");
          out.print("</head>");
          out.print("<body>");
          out.print("Error while applying trace config changes: " + t.toString());
          out.print("</body>");
          out.print("</html>");
          logger.log(Level.WARNING, "error while applying trace config changes", t);
        } catch (Throwable e) {
          logger.log(Level.WARNING, "error while applying trace config changes", e);
          return true;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Apply updated trace configuration through the tracerProvider based on query parameters.
   *
   * @param queryMap the map containing URL query parameters.
   * @throws NumberFormatException if one of the {@code double}/{@code integer} valued query string
   *     does not contain a parsable {@code double}/{@code integer}.
   */
  private void applyTraceConfig(Map<String, String> queryMap) {
    String action = queryMap.get(QUERY_STRING_ACTION);
    if (action == null) {
      return;
    }
    if (action.equals(QUERY_STRING_ACTION_CHANGE)) {
      TraceConfig.Builder newConfigBuilder =
          this.tracerSdkManagement.getActiveTraceConfig().toBuilder();
      String samplingProbabilityStr = queryMap.get(QUERY_STRING_SAMPLING_PROBABILITY);
      if (samplingProbabilityStr != null) {
        try {
          double samplingProbability = Double.parseDouble(samplingProbabilityStr);
          if (samplingProbability == 0) {
            newConfigBuilder.setSampler(Sampler.alwaysOff());
          } else if (samplingProbability == 1) {
            newConfigBuilder.setSampler(Sampler.alwaysOn());
          } else {
            newConfigBuilder.setSampler(Sampler.traceIdRatioBased(samplingProbability));
          }
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("SamplingProbability must be of the type double", e);
        }
      }
      String maxNumOfAttributesStr = queryMap.get(QUERY_STRING_MAX_NUM_OF_ATTRIBUTES);
      if (maxNumOfAttributesStr != null) {
        try {
          int maxNumOfAttributes = Integer.parseInt(maxNumOfAttributesStr);
          newConfigBuilder.setMaxNumberOfAttributes(maxNumOfAttributes);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("MaxNumOfAttributes must be of the type integer", e);
        }
      }
      String maxNumOfEventsStr = queryMap.get(QUERY_STRING_MAX_NUM_OF_EVENTS);
      if (maxNumOfEventsStr != null) {
        try {
          int maxNumOfEvents = Integer.parseInt(maxNumOfEventsStr);
          newConfigBuilder.setMaxNumberOfEvents(maxNumOfEvents);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("MaxNumOfEvents must be of the type integer", e);
        }
      }
      String maxNumOfLinksStr = queryMap.get(QUERY_STRING_MAX_NUM_OF_LINKS);
      if (maxNumOfLinksStr != null) {
        try {
          int maxNumOfLinks = Integer.parseInt(maxNumOfLinksStr);
          newConfigBuilder.setMaxNumberOfLinks(maxNumOfLinks);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("MaxNumOfLinks must be of the type integer", e);
        }
      }
      String maxNumOfAttributesPerEventStr =
          queryMap.get(QUERY_STRING_MAX_NUM_OF_ATTRIBUTES_PER_EVENT);
      if (maxNumOfAttributesPerEventStr != null) {
        try {
          int maxNumOfAttributesPerEvent = Integer.parseInt(maxNumOfAttributesPerEventStr);
          newConfigBuilder.setMaxNumberOfAttributesPerEvent(maxNumOfAttributesPerEvent);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(
              "MaxNumOfAttributesPerEvent must be of the type integer", e);
        }
      }
      String maxNumOfAttributesPerLinkStr =
          queryMap.get(QUERY_STRING_MAX_NUM_OF_ATTRIBUTES_PER_LINK);
      if (maxNumOfAttributesPerLinkStr != null) {
        try {
          int maxNumOfAttributesPerLink = Integer.parseInt(maxNumOfAttributesPerLinkStr);
          newConfigBuilder.setMaxNumberOfAttributesPerLink(maxNumOfAttributesPerLink);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(
              "MaxNumOfAttributesPerLink must be of the type integer", e);
        }
      }
      this.tracerSdkManagement.updateActiveTraceConfig(newConfigBuilder.build());
    } else if (action.equals(QUERY_STRING_ACTION_DEFAULT)) {
      TraceConfig defaultConfig = TraceConfig.getDefault().toBuilder().build();
      this.tracerSdkManagement.updateActiveTraceConfig(defaultConfig);
    }
  }
}
