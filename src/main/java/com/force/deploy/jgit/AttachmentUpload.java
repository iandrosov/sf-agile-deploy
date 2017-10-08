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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.force.deploy.start.DeployEnv;
import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.Attachment;
//import com.sforce.soap.enterprise.sobject.wsc.Attachment;
//import com.sforce.soap.enterprise.wsc.Connector;
//import com.sforce.soap.enterprise.wsc.EnterpriseConnection;
//import com.sforce.soap.enterprise.wsc.SaveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class AttachmentUpload {
	//private static final String TEST_SERVER_URL = "https://test.salesforce.com";
	
	public static void savePackageAttachment(String filePath, String storyId, String sessionId, String apiURL) throws ConnectionException{
		try {
			
	        File f = new File(filePath+"/src/package.xml");
	        InputStream is = new FileInputStream(f);
	        byte[] inbuff = new byte[(int)f.length()];        
	        is.read(inbuff);
	        
	        
	        Attachment attach = new Attachment();
	        attach.setBody(inbuff);
	        attach.setName("package.xml");
	        attach.setIsPrivate(false);
	        // attach to an object in SFDC 
	        attach.setParentId(storyId); 
	        
	        // close file buffer
	        is.close();

		    DeployEnv env = new DeployEnv();    
			String TEST_SERVER_URL = env.getSfPackageHost(); 
	        
			String endpoint = TEST_SERVER_URL+"/services/Soap/c/40.0";  

	        String serverUrl = (apiURL != null) ? apiURL : endpoint;
			ConnectorConfig config = new ConnectorConfig();
			config.setSessionId(sessionId);
			config.setServiceEndpoint(serverUrl);
			EnterpriseConnection connection = Connector.newConnection(config);
			System.out.println("Start added attachment.<br/>");
			//com.sforce.soap.enterprise.sobject.wsc.SObject[] records = new com.sforce.soap.enterprise.sobject.wsc.SObject[] {attach};
			com.sforce.soap.enterprise.sobject.SObject[] records = new com.sforce.soap.enterprise.sobject.SObject[] {attach};
			
	        SaveResult sra[] = connection.create(records);
	        SaveResult sr = sra[0];
	        System.out.println("Complete added attachment.<br/>");
	        if (sr.isSuccess()) {
	            System.out.println("Successfully added attachment.<br/>");
	        } else {
	            System.out.println("Error adding attachment: " + sr.getErrors()[0].getMessage() +"<br/>");
	        }


	    } catch (FileNotFoundException fnf) {
	        System.out.println("File Not Found: " +fnf.getMessage() +"<br/>");

	    } catch (IOException io) {
	        System.out.println("IO: " +io.getMessage() +"<br/>");            
	    }
	}
	
}
