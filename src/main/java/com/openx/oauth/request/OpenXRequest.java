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
package com.openx.oauth.request;

import com.openx.oauth.service.OpenXServiceImpl;
import java.util.HashMap;
import java.util.Map;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

/**
 *
 * @author keithmiller
 */
public class OpenXRequest extends OAuthRequest {

    private static final String OAUTH_PREFIX = "oauth_";
    private Map<String, String> oauthParameters;

    /**
     * Default constructor.
     *
     * @param verb Http verb/method
     * @param url resource URL
     */
    public OpenXRequest(Verb verb, String url) {
        super(verb, url);
        this.oauthParameters = new HashMap<String, String>();
    }

    /**
     * Adds an OAuth parameter.
     *
     * @param key name of the parameter
     * @param value value of the parameter
     *
     * @throws IllegalArgumentException if the parameter is not an OAuth parameter
     */
    @Override
    public void addOAuthParameter(String key, String value) {
        this.oauthParameters.put(checkKey(key), value);
    }

    private String checkKey(String key) {
        if (key.startsWith(OAUTH_PREFIX) || key.equals(OAuthConstants.SCOPE)
                || key.equals(OpenXServiceImpl.getRealmHeader())) {
            return key;
        } else {
            throw new IllegalArgumentException(String.format("OAuth parameters must either be '%s' or start with '%s'", OAuthConstants.SCOPE, OAUTH_PREFIX));
        }
    }

    /**
     * Returns the {@link Map} containing the key-value pair of parameters.
     *
     * @return parameters as map
     */
    @Override
    public Map<String, String> getOauthParameters() {
        return this.oauthParameters;
    }

    /**
     * toString method
     * @return 
     */
    @Override
    public String toString() {
        return String.format("@OpenXRequest(%s, %s)", getVerb(), getUrl());
    }

    /**
     * Remove the value from the parameter map
     * @param key
     */
    public void removeOAuthParameter(String key) {
        this.oauthParameters.remove(key);
    }
}