package com.redcanari.net;


import com.redcanari.burp.WebKitBrowserTab;
import com.redcanari.net.cache.HttpResponseCache;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * Created by ndouba on 2014-06-01.
 */
public class InterceptedHttpURLConnection extends HttpURLConnection {

    private HttpResponseCache httpResponseCache;
    private boolean isIntercepted = false;
    private CachedHttpResponse cachedHttpResponse = null;
    private InputStream inputStream;

    public InterceptedHttpURLConnection(URL url, Proxy proxy) {
        super(url, proxy);
        setUseCaches(false);
        setDefaultUseCaches(false);
        httpResponseCache = HttpResponseCache.getInstance();
    }

    public InterceptedHttpURLConnection(URL url) throws IOException {
        this(url, null);
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    synchronized public void connect() throws IOException {
        System.err.println("Processing request: " + url);
//        url = fixUrlQuery(url);
        if (url.getFile().contains(WebKitBrowserTab.REPEATER_PARAM_NAME) && httpResponseCache.containsKey(url)) {
            System.err.println("Intercepting request: " + url);

            isIntercepted = true;
            cachedHttpResponse = httpResponseCache.get(url);
            responseCode = cachedHttpResponse.getStatusCode();

            try {
                getCookieHandler().put(getURL().toURI(), getHeaderFields());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            inputStream = cachedHttpResponse.getInputStream();
            connected = true;
        } else
            super.connect();
    }

    @Override
    synchronized public Map<String, List<String>> getHeaderFields() {
        if (isIntercepted)
            return cachedHttpResponse.getHeaders();
        return super.getHeaderFields();
    }

    @Override
    synchronized public InputStream getInputStream() throws IOException {
        if (!connected)
            connect();
        if (isIntercepted)
            return inputStream;
        return super.getInputStream();
    }



}