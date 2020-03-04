package io.opentelemetry.sdk.contrib.typedspan;

import io.opentelemetry.sdk.contrib.typedspan.http.HttpURLConnectionWebRequest;
import io.opentelemetry.sdk.contrib.typedspan.http.URLConnectionWebRequest;
import io.opentelemetry.sdk.contrib.typedspan.http.WebRequest;
import io.opentelemetry.trace.Span;
import sun.net.www.protocol.http.HttpURLConnection;

import java.net.URLConnection;

public class TypedSpanFactory {

	public static Span.Builder create(URLConnection connection) {
		String spanName = WebRequest.extractSpanName(connection.getURL());
		String url = connection.getURL().toString();
		String host = connection.getHeaderField("Host");
		String schema = connection.getURL().getProtocol();
		WebRequest wb = new URLConnectionWebRequest(connection, spanName, url, host, schema);
		return wb.build();
	}

	public static Span.Builder create(HttpURLConnection connection) {
		String spanName = WebRequest.extractSpanName(connection.getURL());
		String url = connection.getURL().toString();
		String host = connection.getHeaderField("Host");
		String schema = connection.getURL().getProtocol();
		WebRequest wb = new HttpURLConnectionWebRequest(connection, spanName, url, host, schema);
		return wb.build();
	}

	private TypedSpanFactory(){}

}
