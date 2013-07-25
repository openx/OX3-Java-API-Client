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
package com.openx.oauth.api;

import com.openx.oauth.extractors.HeaderExtractorOpenXImpl;
import com.openx.oauth.service.OpenXServiceImpl;
import org.scribe.builder.api.Api;
import org.scribe.extractors.*;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.scribe.services.*;

/**
 * OpenXAPI Class
 * @author keithmiller
 */
public class OpenXApi implements Api {

    private String requestTokenUrl;
    private String accessTokenUrl;
    private String authorizeUrl;

    /**
     * Class Constructor
     * @param requestTokenUrl
     * @param accessTokenUrl
     * @param authorizeUrl
     */
    public OpenXApi(String requestTokenUrl, String accessTokenUrl,
            String authorizeUrl) {
        this.requestTokenUrl = requestTokenUrl;
        this.accessTokenUrl = accessTokenUrl;
        this.authorizeUrl = authorizeUrl;
    }

    /**
     * Class Constructor
     *
     * DEPRECATED--Realm is now ignored
     * @param requestTokenUrl
     * @param accessTokenUrl
     * @param realm
     * @param authorizeUrl 
     */
    public OpenXApi(String requestTokenUrl, String accessTokenUrl, String realm,
            String authorizeUrl) {
        this.requestTokenUrl = requestTokenUrl;
        this.accessTokenUrl = accessTokenUrl;
        this.authorizeUrl = authorizeUrl;
    }

    /**
     * Returns the access token extractor.
     *
     * @return AccessTokenExtractor token extractor
     */
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new TokenExtractorImpl();
    }

    /**
     * Returns the base string extractor.
     *
     * @return BaseStringExtractor string extractor
     */
    public BaseStringExtractor getBaseStringExtractor() {
        return new BaseStringExtractorImpl();
    }

    /**
     * Returns the header extractor.
     *
     * @return HeaderExtractor extractor
     */
    public HeaderExtractor getHeaderExtractor() {
        return new HeaderExtractorOpenXImpl();
    }

    /**
     * Returns the request token extractor.
     *
     * @return RequestTokenExtractor token extractor
     */
    public RequestTokenExtractor getRequestTokenExtractor() {
        return new TokenExtractorImpl();
    }

    /**
     * Returns the signature service.
     *
     * @return SignatureService service
     */
    public SignatureService getSignatureService() {
        return new HMACSha1SignatureService();
    }

    /**
     * Returns the timestamp service.
     *
     * @return TimestampService service
     */
    public TimestampService getTimestampService() {
        return new TimestampServiceImpl();
    }

    /**
     * Returns the verb for the access token endpoint (defaults to POST)
     *
     * @return Verb access token endpoint verb
     */
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    /**
     * Returns the verb for the request token endpoint (defaults to POST)
     *
     * @return Verb request token endpoint verb
     */
    public Verb getRequestTokenVerb() {
        return Verb.POST;
    }

    /**
     * Returns the URL that receives the access token requests.
     *
     * @return String access token URL
     */
    public String getAccessTokenEndpoint() {
        return this.accessTokenUrl;
    }

    /**
     * Returns the URL that receives the request token requests.
     *
     * @return String request token URL
     */
    public String getRequestTokenEndpoint() {
        return this.requestTokenUrl;
    }

    /**
     * Returns the URL where you should redirect your users to authenticate
     * your application.
     *
     * @param requestToken the request token you need to authorize
     * @return String the URL where you should redirect your users
     */
    public String getAuthorizationUrl(Token requestToken) {
        return String.format(this.authorizeUrl, requestToken.getToken());
    }

    /**
     * Returns the {@link OpenXServiceImpl} for this api
     *
     * @param apiKey key
     * @param apiSecret  secret
     * @param callback OAuth callback (either URL or 'oob')
     * @param scope OAuth scope (optional)
     * @return OpenXServiceImpl 
     */
    @Override
    public OpenXServiceImpl createService(OAuthConfig config, String scope) {
        OpenXServiceImpl service = (OpenXServiceImpl) doCreateService(config);
        service.addScope(scope);
        return service;
    }

    /**
     * Creates the service
     * @param config
     * @return OAuthService
     */
    private OAuthService doCreateService(OAuthConfig config) {
        return new OpenXServiceImpl(this, config);
    }

    /**
     * Returns the authorize url
     * @return String the authorize url
     */
    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    /**
     * Returns the OAuth realm
     *
     * DEPRECATED--Realm is no longer used, so this method always returns null.
     * @return String realm
     */
    public String getRealm() {
        return null;
    }
}