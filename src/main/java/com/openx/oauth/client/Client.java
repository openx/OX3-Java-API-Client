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
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import com.openx.oauth.api.OpenXApi;
import com.openx.oauth.builder.OpenXServiceBuilder;
import com.openx.oauth.service.OpenXServiceImpl;

/**
 * OAuth Java Client
 *
 */
public class Client
{
    /**
     * Path to use if the instance is API v1
     */
    public static final String API_PATH_V1 = "/ox/3.0/a/";

    /**
     * Path to use if the instance is API v2
     */
    public static final String API_PATH_V2 = "/ox/4.0/";

    /**
     * A list of acceptable API paths
     *
     * If the path is not a member of this array, it will be rejected.
     */
    public static final String[] OK_API_PATHS = {API_PATH_V1, API_PATH_V2};

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private final String apiKey;
    private final String apiSecret;
    private final String loginUrl;
    private final String username;
    private final String password;
    private final String domain;
    private final String path;
    private final String requestTokenUrl;
    private final String accessTokenUrl;
    private final String authorizeUrl;
    private Helper helper;
    private HttpHost proxy;
    private boolean ignoreSslCertificate;

    /**
     * Create the OpenX OAuth Client
     *
     * @param apiKey
     * @param apiSecret
     * @param loginUrl
     * @param username
     * @param password
     * @param domain
     * @param path
     * @param requestTokenUrl
     * @param accessTokenUrl
     * @param authorizeUrl
     */
    public Client(
            String apiKey,
            String apiSecret,
            String loginUrl,
            String username,
            String password,
            String domain,
            String path,
            String requestTokenUrl,
            String accessTokenUrl,
            String authorizeUrl )
    {
        this(apiKey,
            apiSecret,
            loginUrl,
            username,
            password,
            domain,
            path,
            requestTokenUrl,
            accessTokenUrl,
            authorizeUrl,
            null,
            false);
    }

    /**
     * Create the OpenX OAuth Client with proxy
     *
     * @param apiKey
     * @param apiSecret
     * @param loginUrl
     * @param username
     * @param password
     * @param domain
     * @param path
     * @param requestTokenUrl
     * @param accessTokenUrl
     * @param authorizeUrl
     * @param proxy
     */
    public Client(
            String apiKey,
            String apiSecret,
            String loginUrl,
            String username,
            String password,
            String domain,
            String path,
            String requestTokenUrl,
            String accessTokenUrl,
            String authorizeUrl,
            HttpHost proxy,
            boolean ignoreSslCertificate)
    {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.loginUrl = loginUrl;
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.path = path;
        this.requestTokenUrl = requestTokenUrl;
        this.accessTokenUrl = accessTokenUrl;
        this.authorizeUrl = authorizeUrl;
        this.proxy = proxy;
        this.ignoreSslCertificate = ignoreSslCertificate;
    }

    /**
     * Create the OpenX OAuth Client
     *
     * DEPRECATED--Realm is now ignored
     * @param apiKey
     * @param apiSecret
     * @param loginUrl
     * @param username
     * @param password
     * @param domain
     * @param path
     * @param requestTokenUrl
     * @param accessTokenUrl
     * @param realm
     * @param authorizeUrl
     */
    @Deprecated
    public Client(
            String apiKey,
            String apiSecret,
            String loginUrl,
            String username,
            String password,
            String domain,
            String path,
            String requestTokenUrl,
            String accessTokenUrl,
            String realm,
            String authorizeUrl )
    {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.loginUrl = loginUrl;
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.path = path;
        this.requestTokenUrl = requestTokenUrl;
        this.accessTokenUrl = accessTokenUrl;
        this.authorizeUrl = authorizeUrl;
    }

    /**
     * Perform the login procedure
     */
    public void OX3OAuth() throws UnsupportedEncodingException,
            IOException, Exception
    {
        // start the OAuth login process
        logger.fine( "Starting OAuth process..." );

        // Override system proxy settings for both http & https connections. Necessary for OAuth.
        if (proxy != null) {
            String proxyHost = proxy.getHostName();
            String proxyPort = String.valueOf(proxy.getPort());
            logger.fine("Using proxyHost = " + proxyHost);
            logger.fine("Using proxyPort = " + proxyPort);
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", proxyPort);
        }

        OpenXApi api = new OpenXApi(requestTokenUrl, accessTokenUrl,
                authorizeUrl);
        OpenXServiceImpl service = new OpenXServiceBuilder()
           .provider(api)
           .apiKey(apiKey)
           .apiSecret(apiSecret)
           .build();

        // get the request token
        Token requestToken = service.getRequestToken();
        logger.fine( "Request Token Output: " + requestToken.toString() );

        // now to log in
        String result;
        helper = new Helper(proxy, loginUrl, username, password, requestToken.getToken(), ignoreSslCertificate);
        result = helper.doLogin();

        logger.fine("SSO Login response: " + result);
        if(result.isEmpty()) {
            throw new Exception( "There was an error logging into the OAuth Server" );
        }

        // process the result from the OAuth server
        Map<String, String> params;
        try {
            params = helper.splitQueryString(
                    result.replace("oob?", ""));
        } catch (UnsupportedEncodingException ex) {
            logger.warning( "You should probably have UTF-8 encoding..." );
            return;
        }

        logger.fine( "Here is the returned token from logging in: " +
                params.get("oauth_token") );

        // get the access token
        Verifier verifier = new Verifier(params.get("oauth_verifier"));
        Token accessToken = service.getAccessToken(requestToken, verifier);

        logger.fine( "Access Token Output: " + accessToken.toString() );

        // now submit the access token to the API to validate
        boolean valid = helper.validateToken(domain,
                    accessToken.getToken(), path);

        if(!valid) {
            throw new Exception("The API could not verify the access token");
        }
    }

    /**
     * Gets the Client helper object
     * @return Helper
     */
    public Helper getHelper() {
        return helper;
    }
}
