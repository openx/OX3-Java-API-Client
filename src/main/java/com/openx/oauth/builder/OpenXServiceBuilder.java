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
package com.openx.oauth.builder;

import com.openx.oauth.api.OpenXApi;
import com.openx.oauth.service.OpenXServiceImpl;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.utils.Preconditions;

/**
 * OpenX Service Builder Class
 * @author keithmiller
 */
public class OpenXServiceBuilder {

    private String apiKey;
    private String apiSecret;
    private String callback;
    private OpenXApi api;
    private String scope;

    /**
     * Object Constructor
     */
    public OpenXServiceBuilder() {
        this.callback = OAuthConstants.OUT_OF_BAND;
    }

    /**
     * Create the API (Does nothing, just exists for compatability)
     * @param api
     * @return OpenXApi
     */
    private OpenXApi createApi(OpenXApi api) {
        return api;
    }

    /**
     * Configures the {@link Api}
     *
     * Overloaded version. Let's you use an instance instead of a class.
     *
     * @param api instance of {@link OpenXApi}s
     * @return the {@link OpenXServiceBuilder} instance for method chaining
     */
    public OpenXServiceBuilder provider(OpenXApi api) {
        Preconditions.checkNotNull(api, "Api cannot be null");
        this.api = api;
        return this;
    }

    /**
     * Adds an OAuth callback url
     * 
     * @param callback callback url. Must be a valid url or 'oob' for out of band OAuth
     * @return the {@link OpenXServiceBuilder} instance for method chaining
     */
    public OpenXServiceBuilder callback(String callback) {
        Preconditions.checkValidOAuthCallback(callback, "Callback must be a valid URL or 'oob'");
        this.callback = callback;
        return this;
    }

    /**
     * Configures the api key
     * 
     * @param apiKey The api key for your application
     * @return the {@link OpenXServiceBuilder} instance for method chaining
     */
    public OpenXServiceBuilder apiKey(String apiKey) {
        Preconditions.checkEmptyString(apiKey, "Invalid Api key");
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Configures the api secret
     * 
     * @param apiSecret The api secret for your application
     * @return the {@link OpenXServiceBuilder} instance for method chaining
     */
    public OpenXServiceBuilder apiSecret(String apiSecret) {
        Preconditions.checkEmptyString(apiSecret, "Invalid Api secret");
        this.apiSecret = apiSecret;
        return this;
    }

    /**
     * Configures the OAuth scope. This is only necessary in some APIs (like Google's).
     * 
     * @param scope The OAuth scope
     * @return the {@link OpenXServiceBuilder} instance for method chaining
     */
    public OpenXServiceBuilder scope(String scope) {
        Preconditions.checkEmptyString(scope, "Invalid OAuth scope");
        this.scope = scope;
        return this;
    }

    /**
     * Returns the fully configured {@link OpenXServiceImpl}
     * 
     * @return fully configured {@link OpenXServiceImpl}
     */
    public OpenXServiceImpl build() {
        Preconditions.checkNotNull(api, "You must specify a valid api through the provider() method");
        Preconditions.checkEmptyString(apiKey, "You must provide an api key");
        Preconditions.checkEmptyString(apiSecret, "You must provide an api secret");
        return api.createService(new OAuthConfig(apiKey, apiSecret, callback), scope);
    }
}