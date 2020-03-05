package io.opentelemetry.sdk.contrib.typedspan.http;

import java.util.HashMap;
import java.util.Map;

// Adapted from org.apache.commons.httpclient.HttpStatus
public final class HttpStatusCode {

  // -------------------------------------------------------------- Constants

  // --- 1xx Informational ---

  /** <tt>100 Continue</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_CONTINUE = 100;
  /** <tt>101 Switching Protocols</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_SWITCHING_PROTOCOLS = 101;
  /** <tt>102 Processing</tt> (WebDAV - RFC 2518) */
  public static final int SC_PROCESSING = 102;

  // --- 2xx Success ---

  /** <tt>200 OK</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_OK = 200;
  /** <tt>201 Created</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_CREATED = 201;
  /** <tt>202 Accepted</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_ACCEPTED = 202;
  /** <tt>203 Non Authoritative Information</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
  /** <tt>204 No Content</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_NO_CONTENT = 204;
  /** <tt>205 Reset Content</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_RESET_CONTENT = 205;
  /** <tt>206 Partial Content</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_PARTIAL_CONTENT = 206;
  /**
   * <tt>207 Multi-Status</tt> (WebDAV - RFC 2518) or <tt>207 Partial Update OK</tt> (HTTP/1.1 -
   * draft-ietf-http-v11-spec-rev-01?)
   */
  public static final int SC_MULTI_STATUS = 207;

  // --- 3xx Redirection ---

  /** <tt>300 Mutliple Choices</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_MULTIPLE_CHOICES = 300;
  /** <tt>301 Moved Permanently</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_MOVED_PERMANENTLY = 301;
  /** <tt>302 Moved Temporarily</tt> (Sometimes <tt>Found</tt>) (HTTP/1.0 - RFC 1945) */
  public static final int SC_MOVED_TEMPORARILY = 302;
  /** <tt>303 See Other</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_SEE_OTHER = 303;
  /** <tt>304 Not Modified</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_NOT_MODIFIED = 304;
  /** <tt>305 Use Proxy</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_USE_PROXY = 305;
  /** <tt>307 Temporary Redirect</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_TEMPORARY_REDIRECT = 307;

  // --- 4xx Client Error ---

  /** <tt>400 Bad Request</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_BAD_REQUEST = 400;
  /** <tt>401 Unauthorized</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_UNAUTHORIZED = 401;
  /** <tt>402 Payment Required</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_PAYMENT_REQUIRED = 402;
  /** <tt>403 Forbidden</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_FORBIDDEN = 403;
  /** <tt>404 Not Found</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_NOT_FOUND = 404;
  /** <tt>405 Method Not Allowed</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_METHOD_NOT_ALLOWED = 405;
  /** <tt>406 Not Acceptable</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_NOT_ACCEPTABLE = 406;
  /** <tt>407 Proxy Authentication Required</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
  /** <tt>408 Request Timeout</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_REQUEST_TIMEOUT = 408;
  /** <tt>409 Conflict</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_CONFLICT = 409;
  /** <tt>410 Gone</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_GONE = 410;
  /** <tt>411 Length Required</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_LENGTH_REQUIRED = 411;
  /** <tt>412 Precondition Failed</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_PRECONDITION_FAILED = 412;
  /** <tt>413 Request Entity Too Large</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_REQUEST_TOO_LONG = 413;
  /** <tt>414 Request-URI Too Long</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_REQUEST_URI_TOO_LONG = 414;
  /** <tt>415 Unsupported Media Type</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
  /** <tt>416 Requested Range Not Satisfiable</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
  /** <tt>417 Expectation Failed</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_EXPECTATION_FAILED = 417;

  /**
   * Static constant for a 418 error. <tt>418 Unprocessable Entity</tt> (WebDAV drafts?) or <tt>418
   * Reauthentication Required</tt> (HTTP/1.1 drafts?)
   */
  // not used
  // public static final int SC_UNPROCESSABLE_ENTITY = 418;

  /**
   * Static constant for a 419 error. <tt>419 Insufficient Space on Resource</tt> (WebDAV -
   * draft-ietf-webdav-protocol-05?) or <tt>419 Proxy Reauthentication Required</tt> (HTTP/1.1
   * drafts?)
   */
  public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
  /**
   * Static constant for a 420 error. <tt>420 Method Failure</tt> (WebDAV -
   * draft-ietf-webdav-protocol-05?)
   */
  public static final int SC_METHOD_FAILURE = 420;
  /** <tt>422 Unprocessable Entity</tt> (WebDAV - RFC 2518) */
  public static final int SC_UNPROCESSABLE_ENTITY = 422;
  /** <tt>423 Locked</tt> (WebDAV - RFC 2518) */
  public static final int SC_LOCKED = 423;
  /** <tt>424 Failed Dependency</tt> (WebDAV - RFC 2518) */
  public static final int SC_FAILED_DEPENDENCY = 424;

  // --- 5xx Server Error ---

  /** <tt>500 Server Error</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_INTERNAL_SERVER_ERROR = 500;
  /** <tt>501 Not Implemented</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_NOT_IMPLEMENTED = 501;
  /** <tt>502 Bad Gateway</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_BAD_GATEWAY = 502;
  /** <tt>503 Service Unavailable</tt> (HTTP/1.0 - RFC 1945) */
  public static final int SC_SERVICE_UNAVAILABLE = 503;
  /** <tt>504 Gateway Timeout</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_GATEWAY_TIMEOUT = 504;
  /** <tt>505 HTTP Version Not Supported</tt> (HTTP/1.1 - RFC 2616) */
  public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

  /** <tt>507 Insufficient Storage</tt> (WebDAV - RFC 2518) */
  public static final int SC_INSUFFICIENT_STORAGE = 507;

  private static final Map<Integer, String> code2text = new HashMap<>();

  // ----------------------------------------------------- Static Initializer

  /** Set up status code to "reason phrase" map. */
  static {
    // HTTP 1.0 Server status codes -- see RFC 1945
    addStatusCodeMap(SC_OK, "OK");
    addStatusCodeMap(SC_CREATED, "Created");
    addStatusCodeMap(SC_ACCEPTED, "Accepted");
    addStatusCodeMap(SC_NO_CONTENT, "No Content");
    addStatusCodeMap(SC_MOVED_PERMANENTLY, "Moved Permanently");
    addStatusCodeMap(SC_MOVED_TEMPORARILY, "Moved Temporarily");
    addStatusCodeMap(SC_NOT_MODIFIED, "Not Modified");
    addStatusCodeMap(SC_BAD_REQUEST, "Bad Request");
    addStatusCodeMap(SC_UNAUTHORIZED, "Unauthorized");
    addStatusCodeMap(SC_FORBIDDEN, "Forbidden");
    addStatusCodeMap(SC_NOT_FOUND, "Not Found");
    addStatusCodeMap(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    addStatusCodeMap(SC_NOT_IMPLEMENTED, "Not Implemented");
    addStatusCodeMap(SC_BAD_GATEWAY, "Bad Gateway");
    addStatusCodeMap(SC_SERVICE_UNAVAILABLE, "Service Unavailable");

    // HTTP 1.1 Server status codes -- see RFC 2048
    addStatusCodeMap(SC_CONTINUE, "Continue");
    addStatusCodeMap(SC_TEMPORARY_REDIRECT, "Temporary Redirect");
    addStatusCodeMap(SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
    addStatusCodeMap(SC_CONFLICT, "Conflict");
    addStatusCodeMap(SC_PRECONDITION_FAILED, "Precondition Failed");
    addStatusCodeMap(SC_REQUEST_TOO_LONG, "Request Too Long");
    addStatusCodeMap(SC_REQUEST_URI_TOO_LONG, "Request-URI Too Long");
    addStatusCodeMap(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
    addStatusCodeMap(SC_MULTIPLE_CHOICES, "Multiple Choices");
    addStatusCodeMap(SC_SEE_OTHER, "See Other");
    addStatusCodeMap(SC_USE_PROXY, "Use Proxy");
    addStatusCodeMap(SC_PAYMENT_REQUIRED, "Payment Required");
    addStatusCodeMap(SC_NOT_ACCEPTABLE, "Not Acceptable");
    addStatusCodeMap(SC_PROXY_AUTHENTICATION_REQUIRED, "Proxy Authentication Required");
    addStatusCodeMap(SC_REQUEST_TIMEOUT, "Request Timeout");

    addStatusCodeMap(SC_SWITCHING_PROTOCOLS, "Switching Protocols");
    addStatusCodeMap(SC_NON_AUTHORITATIVE_INFORMATION, "Non Authoritative Information");
    addStatusCodeMap(SC_RESET_CONTENT, "Reset Content");
    addStatusCodeMap(SC_PARTIAL_CONTENT, "Partial Content");
    addStatusCodeMap(SC_GATEWAY_TIMEOUT, "Gateway Timeout");
    addStatusCodeMap(SC_HTTP_VERSION_NOT_SUPPORTED, "Http Version Not Supported");
    addStatusCodeMap(SC_GONE, "Gone");
    addStatusCodeMap(SC_LENGTH_REQUIRED, "Length Required");
    addStatusCodeMap(SC_REQUESTED_RANGE_NOT_SATISFIABLE, "Requested Range Not Satisfiable");
    addStatusCodeMap(SC_EXPECTATION_FAILED, "Expectation Failed");

    // WebDAV Server-specific status codes
    addStatusCodeMap(SC_PROCESSING, "Processing");
    addStatusCodeMap(SC_MULTI_STATUS, "Multi-Status");
    addStatusCodeMap(SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity");
    addStatusCodeMap(SC_INSUFFICIENT_SPACE_ON_RESOURCE, "Insufficient Space On Resource");
    addStatusCodeMap(SC_METHOD_FAILURE, "Method Failure");
    addStatusCodeMap(SC_LOCKED, "Locked");
    addStatusCodeMap(SC_INSUFFICIENT_STORAGE, "Insufficient Storage");
    addStatusCodeMap(SC_FAILED_DEPENDENCY, "Failed Dependency");
  }

  private static void addStatusCodeMap(int code, String msg) {
    code2text.put(code, msg);
  }

  // instance variable
  int code;

  public HttpStatusCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return code2text.get(this.code);
  }

  public int getCode() {
    return this.code;
  }
}
