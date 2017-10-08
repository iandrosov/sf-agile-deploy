/*
 * sf-agile-deploy - Salesforce Agile Deployment tool
 *
 * Copyright © 2017 Igor Androsov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.
 */

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
