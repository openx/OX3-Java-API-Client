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
package com.openx.oauth.service;

import com.openx.oauth.api.OpenXApi;
import com.openx.oauth.request.OpenXRequest;
import org.scribe.model.*;
import org.scribe.oauth.*;

/**
 * OpenX OauthService Implementor
 * @author keithmiller
 */
public class OpenXServiceImpl implements OAuthService {

    private static final String NO_SCOPE = null;
    private static final String VERSION = "1.0";
    // Deprecated, since realm is no longer used:
    private static final String REALM_HEADER = "OAuth realm";
    private OAuthConfig config;
    private OpenXApi api;
    private String scope;

    /**
     * Default constructor
     *
     * @param api OAuth1.0a api information
     * @param config OAuth 1.0a configuration param object
     */
    public OpenXServiceImpl(OpenXApi api, OAuthConfig config) {
        this.api = api;
        this.config = config;
        this.scope = NO_SCOPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token getRequestToken() {
        OpenXRequest request = new OpenXRequest(api.getRequestTokenVerb(), api.getRequestTokenEndpoint());
        request.addOAuthParameter(OAuthConstants.CALLBACK, config.getCallback());
        addOAuthParams(request, OAuthConstants.EMPTY_TOKEN);
        addOAuthHeader(request);
        request.addBodyParameter(OAuthConstants.CALLBACK, config.getCallback());
        Response response = request.send();
        return api.getRequestTokenExtractor().extract(response.getBody());
    }

    /**
     * Adds params to the OAuth Header
     * @param request
     * @param token 
     */ 
    protected void addOAuthParams(OpenXRequest request, Token token) {
        request.addOAuthParameter(OAuthConstants.TIMESTAMP, api.getTimestampService().getTimestampInSeconds());
        Long nonce = Long.parseLong(api.getTimestampService().getNonce());
        if (nonce < 0) {
            nonce = nonce * -1;
        }

        request.addOAuthParameter(OAuthConstants.NONCE, nonce.toString());
        request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, config.getApiKey());
        request.addOAuthParameter(OAuthConstants.SIGN_METHOD, api.getSignatureService().getSignatureMethod());
        request.addOAuthParameter(OAuthConstants.VERSION, getVersion());
        if (scope == null ? NO_SCOPE != null : !scope.equals(NO_SCOPE)) {
            request.addOAuthParameter(OAuthConstants.SCOPE, scope);
        }
        request.addOAuthParameter(OAuthConstants.SIGNATURE, getSignature(request, token));

        // now to remove the callback param
        request.removeOAuthParameter(OAuthConstants.CALLBACK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token getAccessToken(Token requestToken, Verifier verifier) {
        OpenXRequest request = new OpenXRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        request.addOAuthParameter(OAuthConstants.TOKEN, requestToken.getToken());
        request.addOAuthParameter(OAuthConstants.VERIFIER, verifier.getValue());
        addOAuthParams(request, requestToken);
        addOAuthHeader(request);
        Response response = request.send();
        return api.getAccessTokenExtractor().extract(response.getBody());
    }

    /**
     * {@inheritDoc}
     */
    public void signRequest(Token token, OpenXRequest request) {
        request.addOAuthParameter(OAuthConstants.TOKEN, token.getToken());
        addOAuthParams(request, token);
        addOAuthHeader(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void signRequest(Token token, OAuthRequest request) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addScope(String scope) {
        this.scope = scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return api.getAuthorizationUrl(requestToken);
    }

    /**
     * Gets the signature for the OAuth header
     * @param request
     * @param token
     * @return signature
     */
    protected String getSignature(OpenXRequest request, Token token) {
        String baseString = api.getBaseStringExtractor().extract(request);
        return api.getSignatureService().getSignature(baseString, config.getApiSecret(), token.getSecret());
    }

    /**
     * Adds header to the request
     * @param request 
     */
    protected void addOAuthHeader(OpenXRequest request) {
        String oauthHeader = api.getHeaderExtractor().extract(request);
        request.addHeader(OAuthConstants.HEADER, oauthHeader);
    }

    /**
     * Gets the OAuth realm header key
     * 
     * DEPRECATED--OX3 no longer uses realm
     * @return 
     */
    public static String getRealmHeader() {
        return REALM_HEADER;
    }
}