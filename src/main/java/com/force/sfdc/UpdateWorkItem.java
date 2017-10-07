package com.force.sfdc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.jackson.map.ObjectMapper;

import com.force.deploy.start.DeployEnv;

// This is helper class for use with Agile Accelarator customized package
// update deployment environment custom fields as feedback to users working on 
// work item stories.
//
// Example CURL command works with session id use single quotes around session
// curl https://<YOUR SF INSTANCE URL>/services/data/v39.0/sobjects/agf__ADM_Work__c/a1s610000026p9TAAQ?fields=agf__Environment__c -H 'Authorization: Bearer 00D61000000Ju34!AQIAQOFf5Sk8j9aOV5u8qJdOKFXj2yI0jQLQlqbw72eiGJbZ8TzBE6CnaeYqJ4KTUmyr6JCY1XjOJsKlf3mnDbAzj367bEs8'
//
// author: A.I.
public class UpdateWorkItem {

	private static String getEndpointGET(String storyId){
	    // Get settings from system Environment
	    DeployEnv env = new DeployEnv();    
		String TEST_SERVER_URL = env.getSfPackageHost();		
		String endpoint = TEST_SERVER_URL+"/services/data/v39.0/sobjects/agf__ADM_Work__c/"+storyId+"?fields=agf__Environment__c";
		return endpoint;
	}

	private static String getEndpointPATCH(String storyId){
	    // Get settings from system Enviornment
	    DeployEnv env = new DeployEnv();    
		String TEST_SERVER_URL = env.getSfPackageHost();		
		String endpoint = TEST_SERVER_URL+"/services/data/v39.0/sobjects/agf__ADM_Work__c/"+storyId+"?_HttpMethod=PATCH";
		return endpoint;
	}
	
	public static String getEnvValues(String storyId, String sid) {
		String val = "";
		String endpoint = getEndpointGET(storyId);
		System.out.println("Work ITEM GET URL:"+endpoint);
		
		try {
			URL url = new URL(endpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer "+sid);
	
			
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : "
				    + conn.getResponseCode());
				}
			
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
	
			String output;
			String jsonInString = "";
			System.out.println("Output from Server ....");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				jsonInString += output;
			}
			conn.disconnect();

			// Parse Json to get value
			ObjectMapper mapper = new ObjectMapper();
			//JSON from String to Object
			ADM_WorkObj work_item = mapper.readValue(jsonInString, ADM_WorkObj.class);
			if(work_item != null){
				val = work_item.agf__Environment__c;
			}
	  } catch (MalformedURLException e) {

		e.printStackTrace();

	  } catch (IOException e) {
			e.printStackTrace();
	  
	  }
		return val;
	}
	//throws IOException
	public static String updateStory(String storyId, String sid, String deployTarget)  {
		// Get LIST of env values: "QA; UAT; Personal Dev"
		String env = getEnvValues(storyId, sid); 
		
		if (isEnvSet(env, deployTarget)){
			return ""; // not calling update data is set
		}
		
		String endpoint = getEndpointPATCH(storyId);
		System.out.println("URL:"+endpoint);
		String val = getTargetEnvValues(env,deployTarget);
		try {
			URL url = new URL(endpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			//conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Bearer "+sid);		
			
			System.out.println("Update ENV: "+val);
			//String input = "{ \"id\" : \"" + storyId + "\",";	   
		    String input =  "{ \"agf__Environment__c\" :  \"" + val + "\"}";
		
	
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
		
			// If NOT 200 error
			//if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			//	throw new RuntimeException("Failed : HTTP error code : "
			//		+ conn.getResponseCode());
			//}
		
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		
			String output;
			System.out.println("<br/>Output from Server .... ");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
		
			conn.disconnect();
		
		} catch (MalformedURLException e) {
		
			e.printStackTrace();
		
		} catch (IOException e) {
		
			e.printStackTrace();
		
		}	
		return val;
	}
	// Combine ENV values
	private static String getTargetEnvValues(String env, String deployTarget){
		String val = env;
		String temp = transformDeployTarget(deployTarget);
		val += ";" + temp;
		
		return val;
	}
	// Check if ENV already exists no need ot call API
	// default to false - NOT set need to add ENV
	private static boolean isEnvSet(String env, String deployTarget){
		boolean rc = false;
		String temp = transformDeployTarget(deployTarget);
		System.out.println("ENV TEST: "+temp+" LIST:"+env);
		String[] items = env.split(";");
	    for(int i=0; i < items.length; i++){
	    	if (temp.equalsIgnoreCase(items[i])){
	    		rc = true;
	    		
	    		System.out.println("ITEM:"+items[i]);
	    	}
	    }
		
		return rc;
	}
	private static String transformDeployTarget(String deployTarget){
		String temp = "QA";
		if (deployTarget == "SKYWAYQA"){
			temp = "QA";
		}
		if (deployTarget == "SKYWAYUAT"){
			temp = "UAT";
		}
		if (deployTarget == "PROD"){
			temp="Production";
		}
		return temp;
	}
}
