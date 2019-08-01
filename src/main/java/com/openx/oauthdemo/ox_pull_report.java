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
public class ox_pull_report {

    private static final Logger logger = Logger.getLogger(ox_pull_report.class.getName());
	private static String myParams;
    
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
        path = "/data/1.0/report/";
        requestTokenUrl = defaultProps.getProperty("requestTokenUrl").trim();
        accessTokenUrl = defaultProps.getProperty("accessTokenUrl").trim();
        authorizeUrl = defaultProps.getProperty("authorizeUrl").trim();
        myParams = defaultProps.getProperty("myParams");

        // log in to the server
        Client cl = new Client(apiKey, apiSecret, loginUrl, username, password,
                domain, path, requestTokenUrl, accessTokenUrl, authorizeUrl);
        try {
            // connect to the server
            cl.OX3OAuth();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ox_pull_report.class.getName()).log(Level.SEVERE, "UTF-8 support needed for OAuth", ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(ox_pull_report.class.getName()).log(Level.SEVERE, "IO file reading error", ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        } catch (Exception ex) {
            Logger.getLogger(ox_pull_report.class.getName()).log(Level.SEVERE, "API issue", ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }

        // now lets make a call to the api to check 
        String json = "";
 
        try {
            json = cl.getHelper().postAPICall(domain, path, myParams);
        } catch (IOException ex) {
            logger.warning("There was an error calling the API");
            ex.printStackTrace(System.err);
            System.exit(1);
        }

        // Read out the raw HTTP response body:
        logger.info("JSON response: " + json);

    }
}
