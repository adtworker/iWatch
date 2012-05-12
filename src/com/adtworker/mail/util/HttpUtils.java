package com.adtworker.mail.util;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.net.Proxy;

public class HttpUtils {

	private static HttpClient httpClient;

	public static void initHttpClient() {

		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 100);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		// Create and initialize scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);

		httpClient = new DefaultHttpClient(cm, params);
		// httpClient = new DefaultHttpClient();

		// 判断是否使用代理
		if (Proxy.getDefaultHost() != null) {
			HttpHost proxy = new HttpHost(Proxy.getDefaultHost(),
					Proxy.getDefaultPort());
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}
	}
	public static HttpClient getHttpClient() {
		if (httpClient == null) {
			initHttpClient();
		}
		return httpClient;
	}
	public static String executeGet(String requestUrl) throws Exception {
		if (httpClient == null) {
			initHttpClient();
		}
		HttpGet httpGet = new HttpGet(requestUrl);
		httpGet.addHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.106 Safari/535.2");
		httpGet.addHeader(
				"Referer",
				"http://www.google.com.hk/search?hl=zh-CN&newwindow=1&safe=strict&biw=1399&bih=725&tbs=isz%3Aex%2Ciszw%3A480%2Ciszh%3A800&tbm=isch&sa=1&q=MM+%E5%A3%81%E7%BA%B8&oq=MM+%E5%A3%81%E7%BA%B8&aq=f&aqi=&aql=&gs_l=img.3...680499l683087l0l683551l5l5l0l0l0l0l0l0ll0l0.frgbld.");

		httpClient.getConnectionManager().closeExpiredConnections();
		HttpResponse response = httpClient.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();
		if (200 == statusLine.getStatusCode()) {
			String result = EntityUtils.toString(response.getEntity());
			return result;
		} else {
			return "";
		}
	}

	public static HttpURLConnection getConnection(String url) throws Exception {
		URL u = new URL(url);
		HttpURLConnection connection = null;
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (proxyHost != null) {
			java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP,
					new InetSocketAddress(android.net.Proxy.getDefaultHost(),
							android.net.Proxy.getDefaultPort()));
			connection = (HttpURLConnection) u.openConnection(p);
		} else {
			connection = (HttpURLConnection) u.openConnection();
		}
		connection.setConnectTimeout(5000);
		return connection;
	}
}
