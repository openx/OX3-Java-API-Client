/*======================================================================*
 * Copyright (c) 2011, OpenX Technologies, Inc. All rights reserved.    *
 *                                                                      *
 * Licensed under the New BSD License (the "License"); you may not use  *
 * this file except in compliance with the License. Unless required     *
 * by applicable law or agreed to in writing, software distributed      *
 * under the License is distributed on an "AS IS" BASIS, WITHOUT        *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     *
 * See the License for the specific language governing permissions and  *
 * limitations under the License. See accompanying LICENSE file.        *
 *======================================================================*/
package com.openx.oauth.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.openx.oauth.redirect.OpenXRedirectStrategy;

/**
 * Client Helper class
 * @author keithmiller
 */
public class Helper {
    private static final Logger logger = Logger.getLogger(Helper.class.getName());
    protected HttpHost proxy;
    protected String url;
    protected String username;
    protected String password;
    protected String token;
    protected BasicCookieStore cookieStore;
    private final boolean ignoreSslCertificate;

    /**
     * Object Constructor
     * @param url
     * @param username
     * @param password
     * @param token
     */
    public Helper(String url, String username, String password, String token) {
        this(null, url, username, password, token, false);
    }

    /**
     * Object Constructor
     * @param proxy the proxy to use for the request
     * @param url
     * @param username
     * @param password
     * @param token
     */
    public Helper(HttpHost proxy, String url, String username, String password, String token, boolean ignoreSslCertificate) {
        this.proxy = proxy;
        this.url = url;
        this.username = username;
        this.password = password;
        this.token = token;
        this.ignoreSslCertificate = ignoreSslCertificate;
        this.cookieStore = null;
    }

    /**
     * Log in to the OpenX OAuth server
     * @return String login string
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String doLogin() throws UnsupportedEncodingException, IOException {
        DefaultHttpClient httpclient = openxHttpClientFactory();

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", username));
        formparams.add(new BasicNameValuePair("password", password));
        formparams.add(new BasicNameValuePair("oauth_token", token));
        UrlEncodedFormEntity formEntity =
                new UrlEncodedFormEntity(formparams, "UTF-8");

        HttpPost httpost = new HttpPost(this.url);
        httpost.setEntity(formEntity);

        HttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();

        String result;
        if (response.getStatusLine().getStatusCode() == 200) {
            result = EntityUtils.toString(entity);
        } else {
            result = "";
        }

        httpclient.getConnectionManager().shutdown();
        return result;
    }

    /**
     * Parses the query string
     * @param query string
     * @return Map the query string as a map
     * @throws UnsupportedEncodingException
     */
    public Map<String, String> splitQueryString(String query) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = URLDecoder.decode(pair[1], "UTF-8");
            String values = params.get(key);
            if (values == null) {
                params.put(key, value);
            }
        }

        return params;
    }

    /**
     * Validates the access token with the API
     * @param domain
     * @param token
     * @param path
     * @return success or fail
     * @throws IOException
     */
    public boolean validateToken(String domain, String token, String path)
            throws IOException {
        DefaultHttpClient httpclient = openxHttpClientFactory();

        if (cookieStore == null) {
            createCookieStore(domain, token);
        }

        httpclient.setCookieStore(cookieStore);

        // This extra validation step is only needed for v1:
        if (path.equals(Client.API_PATH_V1)) {
            HttpPut httpput = new HttpPut(domain + path + "session/validate");
            HttpResponse response = httpclient.execute(httpput);

            httpclient.getConnectionManager().shutdown();

            if (response.getStatusLine().getStatusCode() != 200) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates the OX3 API cookie store
     * @param domain
     * @param value
     */
    protected void createCookieStore(String domain, String value) {
        if(cookieStore == null) {
            cookieStore = new BasicCookieStore();
        }
        BasicClientCookie cookie = new BasicClientCookie(
                "openx3_access_token", value);
        cookie.setVersion(0);
        cookie.setDomain(domain.replace("http://", ""));
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
    }

    /**
     * Calls the OX3 API to get a list of objects
     * @param domain
     * @param path
     * @param OX3Entity
     * @return results from the API
     * @throws IOException
     */
    public String callOX3Api(String domain, String path, String OX3Entity)
            throws IOException {
        String request = domain + path + OX3Entity;
        return makeAPICall(domain, request);
    }

    /**
     * Calls the OX3 API to get a list of objects with extra params
     * @param domain
     * @param path
     * @param OX3Entity
     * @param params
     * @return results from the API
     * @throws IOException
     */
    public String callOX3Api(String domain, String path, String OX3Entity,
            String params)
            throws IOException {
        String request = domain + path + OX3Entity + "?" + params;
        return makeAPICall(domain, request);
    }

    /**
     * Calls the OX3 API with an object by an id
     * @param domain
     * @param path
     * @param OX3Entity
     * @param id
     * @return results from the API
     * @throws IOException
     */
    public String callOX3Api(String domain, String path, String OX3Entity,
            int id) throws IOException {
        String request = domain + path + OX3Entity + "/" + id;
        return makeAPICall(domain, request);
    }

    /**
     * This doesn't work as expected since params is not used
     * @param domain
     * @param path
     * @param OX3Entity
     * @param id
     * @param params
     * @return results from the API
     * @throws IOException
     */
    @Deprecated
    public String callOX3Api(String domain, String path, String OX3Entity,
            int id, String params) throws IOException {
        // This looks like it's not even using params
        String request = domain + path + OX3Entity + "/" + id;
        return makeAPICall(domain, request);
    }

    /**
     * Makes the actual API call
     * @param domain
     * @param request
     * @return results from the API
     * @throws IOException
     */
    protected String makeAPICall(String domain, String request)
            throws IOException {
        DefaultHttpClient httpclient = openxHttpClientFactory();

        if (cookieStore == null) {
            createCookieStore(domain, token);
        }

        httpclient.setCookieStore(cookieStore);
        HttpGet httpget = new HttpGet(request);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        String result;
        if (response.getStatusLine().getStatusCode() == 200) {
            result = EntityUtils.toString(entity);
        } else {
            result = "";
        }
        return result;
    }

    /**
     * Getter for the cookieStore
     * @return cookieStore
     */
    public BasicCookieStore getCookieStore()
            throws IllegalAccessException {
        if(cookieStore == null) {
            throw new IllegalAccessException("You must call createCookieStore() "
                    + "before you can access the cookieStore!");
        }
        return cookieStore;
    }

    /**
     * Make post api call using a json string
     *
     * @param domain
     * @param path
     * @param jsonString
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String postAPICall(String domain, String path, String jsonString)
            throws IOException {
        DefaultHttpClient httpclient = openxHttpClientFactory();

        if (cookieStore == null) {
            createCookieStore(domain, token);
        }

        httpclient.setCookieStore(cookieStore);
        HttpPost httppost = new HttpPost(domain + path);
        StringEntity requestEntity = new StringEntity(jsonString);
        requestEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                "application/json"));
        httppost.setEntity(requestEntity);
        HttpResponse response = httpclient.execute(httppost);

        String result = null;
        if (response.getStatusLine().getStatusCode() == 200) {
            result = EntityUtils.toString(response.getEntity());
        } else {
            throw new IOException("RETURNCODE:"
                    + response.getStatusLine().getStatusCode());
        }
        return result;
    }

    private DefaultHttpClient openxHttpClientFactory() {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        if (proxy != null) {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        if (ignoreSslCertificate) {
            logger.info("disable ssl certificate check");
            ignoreSslCertificate(httpclient);
        }
        OpenXRedirectStrategy dsr = new OpenXRedirectStrategy();
        httpclient.setRedirectStrategy(dsr);
        return httpclient;
    }

    /**
     * apache httpclient use https proxy: peer not authenticated
     */
    private void ignoreSslCertificate(DefaultHttpClient httpclient) {
        try {
            SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }, new X509HostnameVerifier(){

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                    // do nothing
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                    // do nothing
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                    // do nothing
                }
            });
            httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sslsf));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
