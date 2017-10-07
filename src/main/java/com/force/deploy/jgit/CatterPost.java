package com.force.deploy.jgit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.force.deploy.start.DeployEnv;

// REST CHATTER API DOCS
// https://developer.salesforce.com/docs/atlas.en-us.chatterapi.meta/chatterapi/quickreference_post_feed_item.htm
//
//
public class CatterPost {

	//private static final String TEST_SERVER_URL = "https://test.salesforce.com";
	
	public static void postChatterStatus(String msg, String feedId, String userId, String sessionId){
		  try {
		    // Get settings from system Enviornment
		    DeployEnv env = new DeployEnv();    
			String TEST_SERVER_URL = env.getSfPackageHost();
			// Service call URL
			String endpoint = TEST_SERVER_URL+"/services/data/v40.0/chatter/feed-elements?feedElementType=FeedItem&subjectId="+feedId+"&text=New+post";  
			System.out.println("URL:"+endpoint+"<br/>");
			URL url = new URL(endpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer "+sessionId);
			
			String input = "{ \"body\" : {";	   
				   input +=  "\"messageSegments\" : [{";
				   input +=  "\"type\" : \"Text\",";
				   input +=  "\"text\" : \""+msg+" \"}";
				   input +=  ",{\"type\" : \"Mention\",\"id\" : \""+userId+"\"}";
				   input +=  "]},\"feedElementType\" : \"FeedItem\",";
				   input +=  "\"subjectId\" : \""+ feedId +"\"}";
				
			
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode() +" msg: "+conn.getResponseMessage());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... ");
			while ((output = br.readLine()) != null) {
				//System.out.println(output+"<br/>");
				output = output + "<br/>";
			}

			conn.disconnect();

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		 }	
		
	}
}
