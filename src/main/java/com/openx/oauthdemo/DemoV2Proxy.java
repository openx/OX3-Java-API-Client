package com.openx.oauthdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.openx.oauth.client.Client;
import com.openx.ox3.entities.OX3Account;

/**
 * OX3 with OAuth demo
 *
 * @author zhentao.li
 *
 */
public class DemoV2Proxy {
    private static final Logger logger = Logger.getLogger(DemoV2Proxy.class.getName());

    /**
     * Main class. OX3 with OAuth demo for proxy server
     *
     * @param args
     */
    public static void main(String[] args) {

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

        String apiKey = defaultProps.getProperty("apiKey").trim();
        String apiSecret = defaultProps.getProperty("apiSecret").trim();
        String loginUrl = defaultProps.getProperty("loginUrl").trim();
        String username = defaultProps.getProperty("username").trim();
        String password = defaultProps.getProperty("password").trim();
        String domain = defaultProps.getProperty("domain").trim();
        String path = defaultProps.getProperty("path").trim();
        String requestTokenUrl = defaultProps.getProperty("requestTokenUrl").trim();
        String accessTokenUrl = defaultProps.getProperty("accessTokenUrl").trim();
        String authorizeUrl = defaultProps.getProperty("authorizeUrl").trim();

        //create proxy host
        String proxyHost = defaultProps.getProperty("proxyHost").trim();
        String proxyPort = defaultProps.getProperty("proxyPort").trim();
        String proxyScheme = defaultProps.getProperty("proxyScheme").trim();
        HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), proxyScheme);
        String ignoreSslCertificate = defaultProps.getProperty("ignoreSslCertificate");
        if (ignoreSslCertificate != null) {
            ignoreSslCertificate = ignoreSslCertificate.trim();
        }

        // log in to the server
        Client cl = new Client(apiKey, apiSecret, loginUrl, username, password, domain, path, requestTokenUrl,
                accessTokenUrl, authorizeUrl, proxy, Boolean.parseBoolean(ignoreSslCertificate));
        try {
            // connect to the server
            cl.OX3OAuth();
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "UTF-8 support needed for OAuth", ex);
            ex.printStackTrace();
            System.exit(1);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IO file reading error", ex);
            ex.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "API issue", ex);
            ex.printStackTrace();
            System.exit(1);
        }

        // now lets make a call to the api to check
        String json;
        try {
            json = cl.getHelper().callOX3Api(domain, path, "account");
        } catch (IOException ex) {
            logger.warning("There was an error calling the API");
            return;
        }

        // Read out the raw HTTP response body:
        logger.info("JSON response: " + json);

        Gson gson = new Gson();
        // List of actual accounts in this response:
        JsonParser parser = new JsonParser();
        JsonArray accounts = parser.parse(json).getAsJsonObject().getAsJsonArray("objects");

        if (accounts.size() > 0) {
            // let's get a single account
            try {
                // Get the ID of the first account in the list:
                int accountId = accounts.get(0).getAsJsonObject().get("id").getAsInt();
                // Repeat the API query, asking only for the
                // account with this ID:
                json = cl.getHelper().callOX3Api(domain, path, "account", accountId);
            } catch (IOException ex) {
                logger.warning("There was an error calling the API");
                return;
            }

            logger.info("JSON response: " + json);

            // In v2, all responses for single objects come in
            // the form of unary arrays:
            OX3Account account = gson.fromJson(parser.parse(json).getAsJsonArray().get(0), OX3Account.class);

            logger.warning("Account id: " + account.getId() + " name: " + account.getName());
        }
    }
}
