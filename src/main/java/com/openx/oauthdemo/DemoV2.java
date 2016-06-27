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
package com.openx.oauthdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.openx.oauth.client.Client;
import com.openx.ox3.entities.OX3Account;

/**
 * OX3 with OAuth demo
 * @author keithmiller
 */
public class DemoV2 {

    private static final Logger logger = Logger.getLogger(DemoV2.class.getName());
    
    /** 
     * Main class. OX3 with OAuth demo
     * @param args 
     */
    public static void main(String[] args) {
        String apiKey, apiSecret, loginUrl, username, password, domain, path,
                requestTokenUrl, accessTokenUrl, authorizeUrl;
        String propertiesFile = "default.properties";

        // load params from the properties file
        Properties defaultProps = new Properties();
        InputStream in = null;
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            in = cl.getResourceAsStream(propertiesFile);
            if (in != null) {
                defaultProps.load(in);
            }
        } catch (IOException ex) {
            logger.warning("The properties file was not found!");
            return;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    logger.warning("IO Error closing the properties file");
                    return;
                }
            }
        }

        if (defaultProps.isEmpty()) {
            logger.warning("The properties file was not loaded!");
            return;
        }

        apiKey = defaultProps.getProperty("apiKey").trim();
        apiSecret = defaultProps.getProperty("apiSecret").trim();
        loginUrl = defaultProps.getProperty("loginUrl").trim();
        username = defaultProps.getProperty("username").trim();
        password = defaultProps.getProperty("password").trim();
        domain = defaultProps.getProperty("domain").trim();
        path = defaultProps.getProperty("path").trim();
        requestTokenUrl = defaultProps.getProperty("requestTokenUrl").trim();
        accessTokenUrl = defaultProps.getProperty("accessTokenUrl").trim();
        authorizeUrl = defaultProps.getProperty("authorizeUrl").trim();

        // log in to the server
        Client cl = new Client(apiKey, apiSecret, loginUrl, username, password,
                domain, path, requestTokenUrl, accessTokenUrl, authorizeUrl);
        try {
            // connect to the server
            cl.OX3OAuth();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Demo.class.getName()).log(Level.SEVERE,
                    "UTF-8 support needed for OAuth", ex);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(Demo.class.getName()).log(Level.SEVERE,
                    "IO file reading error", ex);
            System.exit(1);
        } catch (Exception ex) {
            Logger.getLogger(Demo.class.getName()).log(Level.SEVERE,
                    "API issue", ex);
            System.exit(1);
        }

        // now lets make a call to the api to check 
        String json = "";
        try {
            json = cl.getHelper().callOX3Api(domain, path, "account");
        } catch (IOException ex) {
            logger.warning("There was an error calling the API");
            System.exit(1);
        }

        // Read out the raw HTTP response body:
        logger.info("JSON response: " + json);

        Gson gson = new Gson();
        // List of actual accounts in this response:
        JsonParser parser = new JsonParser();
        JsonArray accounts = parser.parse(json).getAsJsonObject().
                getAsJsonArray("objects");

        if (accounts.size() > 0) {
            // let's get a single account
            try {
                // Get the ID of the first account in the list:
                int accountId = accounts.get(0).getAsJsonObject().get("id").
                        getAsInt();
                // Repeat the API query, asking only for the
                // account with this ID:
                json = cl.getHelper().callOX3Api(domain, path, "account",
                        accountId);
            } catch (IOException ex) {
                logger.warning("There was an error calling the API");
                return;
            }

            logger.info("JSON response: " + json);

            // In v2, all responses for single objects come in
            // the form of unary arrays:
            OX3Account account = gson.fromJson(
                    parser.parse(json).getAsJsonArray().get(0),
                    OX3Account.class);

            logger.warning("Account id: " + account.getId() + " name: "
                    + account.getName());
        }
    }
}
