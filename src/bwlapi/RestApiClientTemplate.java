package bwlapi;

	/* 
	 * Licensed Materials - Property of IBM Corporation.
	 * 
	 * 5725-A20
	 * 
	 * Copyright IBM Corporation 2017. All Rights Reserved.
	 * 
	 * US Government Users Restricted Rights - Use, duplication or disclosure
	 * restricted by GSA ADP Schedule Contract with IBM Corporation.
	 */
	import java.io.IOException;
	import java.io.InputStream;
	import java.net.HttpURLConnection;
	import java.net.URL;

	import javax.xml.bind.DatatypeConverter;

	import org.apache.wink.json4j.JSONArray;
	import org.apache.wink.json4j.JSONException;
	import org.apache.wink.json4j.JSONObject;

	/**
	 * This Java template shows how you might call the Blueworks Live REST APIs.
	 * 
	 * The template uses the JSON4J library from the Apache Wink project (http://wink.apache.org/) to decode the
	 * JSON responses sent back by the API.
	 * 
	 * 1. Download the JAR from http://repo1.maven.org/maven2/org/apache/wink/wink-json4j/1.3.0/wink-json4j-1.3.0.jar
	 * 
	 * 2. Compile the sample (The following code assumes you are using the Windows command prompt):
	 *    javac -cp .;wink-json4j-1.3.0.jar RestApiClientTemplate.java
	 * 
	 * 3. Run it, changing the credentials to something valid:
	 *    java -cp .;wink-json4j-1.3.0.jar RestApiClientTemplate
	 * 
	 * You can use your favorite JSON library.
	 * 
	 */
	public class RestApiClientTemplate {

	    /*
	     * The Blueworks Live server to access the APIs from.
	     */
	    private final static String REST_API_SERVER = "https://ibm.blueworkslive.com";

	    /*
	     * The UserList API call syntax. This API and the others EXCEPT Auth are protected by HTTP Basic Authentication.
	     */
	    private final static String REST_API_CALL_USERLIST = REST_API_SERVER + "/scr/api/Auth";

	    /*
	     * The version of the API you want to use. Different versions of the API require different input parameters and
	     * return results in different formats.
	     */
	    private final static String REST_API_USERLIST_VERSION = "20110917";

	    /*
	     * The user name and password credentials for the user accessing the REST APIs. This example hard codes the
	     * values for ease of instruction, but in reality, you would use a robust approach that can accommodate change. 
	     * For example, you could prompt for them or retrieve them from an external database.
	     */
	    private final static String REST_API_USERNAME = "ncrowther@uk.ibm.com";
	    private final static String REST_API_PASSWORD = "percyPorker01!";

	    /*
	     * The name of the account the above user exists in. This value is required if the user exists in more than one
	     * account. If the user exists in only one account, you can set this value to null.
	     */
	    private final static String REST_API_ACCOUNT_NAME = null;

	    public static void main(String[] args) {

	        try {
	            // Call the REST APIs. In this example, you are calling the API to return the list of users.
	            StringBuilder appListUrlBuilder = new StringBuilder(REST_API_CALL_USERLIST);
	            appListUrlBuilder.append("?version=").append(REST_API_USERLIST_VERSION);

	            // If the user exists in more than one account, specify the account to use when authenticating.
	            if (REST_API_ACCOUNT_NAME != null) {
	                appListUrlBuilder.append("&account=").append(REST_API_ACCOUNT_NAME);
	            }

	            HttpURLConnection restApiURLConnection = getRestApiConnection(appListUrlBuilder.toString());
	            if (restApiURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
	                System.err.println("Error calling the Blueworks Live REST API: " + restApiURLConnection.getResponseMessage());
	                System.exit(1);
	            }

	            // Process the JSON result. This example prints the name of each user.
	            InputStream restApiStream = restApiURLConnection.getInputStream();
	            try {
	                JSONObject appListResult = new JSONObject(restApiStream);
	                JSONArray users = (JSONArray) appListResult.get("users");
	                for (Object user : users) {
	                    System.out.println("User name=" + ((JSONObject) user).get("name"));
	                }
	            } finally {
	                // Clean up the streams you opened.
	                restApiStream.close();
	            }

	        } catch (Exception e) {
	            // Handle the exceptions that might occur.
	            // Perform exception handling suited to your application, which might include distinguishing 
	            // the type of exception and handling it appropriately. For example, you might want to handle
	            // authentication problems separately so that the user will know their credentials caused the problem.
	            e.printStackTrace();
	        }
	    }

	    /**
	     * Set up the connection to a REST API including handling the Basic Authentication request headers that must be
	     * present on every API call.
	     * 
	     * @param apiCall The URL string indicating the API call and parameters.
	     * @return the open connection
	     */
	    public static HttpURLConnection getRestApiConnection(String apiCall) throws IOException {

	        // Call the provided API on the Blueworks Live server.
	        URL restApiUrl = new URL(apiCall);
	        HttpURLConnection restApiURLConnection = (HttpURLConnection) restApiUrl.openConnection();

	        // Add the HTTP Basic Authentication header that must be present on every API call.
	        addAuthenticationHeader(restApiURLConnection);

	        return restApiURLConnection;
	    }

	    /**
	     * Add the HTTP Basic Authentication header that must be present on every API call.
	     * 
	     * @param restApiURLConnection The open connection to the REST API.
	     */
	    private static void addAuthenticationHeader(HttpURLConnection restApiURLConnection) {
	        String userPwd = REST_API_USERNAME + ":" + REST_API_PASSWORD;
	        String encoded = DatatypeConverter.printBase64Binary(userPwd.getBytes());
	        restApiURLConnection.setRequestProperty("Authorization", "Basic " + encoded);
	    }
	}

