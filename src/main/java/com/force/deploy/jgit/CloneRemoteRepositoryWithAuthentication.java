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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.force.deploy.jgit.CloneRemoteRepositoryWithAuthentication;
import com.force.deploy.start.DeployEnv;
import com.force.deploy.start.DeployLog;
import com.force.sfdc.UpdateWorkItem;
import com.sforce.ws.ConnectionException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

// GIthub API
//import org.eclipse.jgit.api.CheckoutCommand;


public class CloneRemoteRepositoryWithAuthentication {
		
	private static final String WORKING_DIR = "deploystore/"; 
	// Dummy session ID not valid for use
	private static final String SESSION_ID = "00D4C0000000eee!ARcAQKdfbYQbNDennacI8kdHh9sN0CpbV2aOcJ8EcCA6w3pFBdUmDrtUVupDWfMwLmjDkgmkP6oXAYa.uhBsDNjhIBjAYwos";
	
	
    public static String cloneRepo(String m_storyId, 
    								String m_storyName, 
    								String m_assignedId,
    								String m_sandbox, // Source sandbox AIDEV, SKYWAYDEV
    								String m_target,  // Target Sandbox/s SKYWAYQA, SKYWAYUAT
    								String m_sessionId,
    								String m_apiURL, DeployLog dlog) throws IOException, GitAPIException, ConnectionException {
    	
    	final Logger logger = LoggerFactory.getLogger(CloneRemoteRepositoryWithAuthentication.class);
    	
    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();    
    	
    	String gitRemoteURL = env.getGitRemoteURL();
    	String gitUser = env.getGitUser();
    	String gitPass = env.getGitPass();   

    	String sessionId = (m_sessionId != null) ? m_sessionId : SESSION_ID;
        String storyName = (m_storyName != null) ? m_storyName : "W-003243"; // Sample work item story name in Agile Tool
        String storyId = (m_storyId != null) ? m_storyId : "a1s4C000000HLFmQAO"; // Example AA Work Item id 
        String assignedId = (m_assignedId != null) ? m_assignedId : "00561000001Z262AAC"; // Example assign Story user Id
        String serverUrl = m_apiURL;
        String sanboxName = (m_sandbox != null) ? m_sandbox : "comdev"; // dummy default sandbox name
    	String targetSBName = (m_target != null) ? m_target : "SKYWAYQA"; // dummy default sandbox name

    	// Make chatter post TEXT
    	String chatterPost = "Story "+ storyName + " deployed to "+targetSBName;

        String strlog = "*** START INITIAL COMMIT DEPLOYMENT Process - "+storyName+" ***\r\n\r\n";
        dlog.info(storyName, strlog, logger);
        // Variabels
        String metaPath = getGitRepoPath(storyName);  
        // Delete existing folder repository
        File storyDir = new File( WORKING_DIR + storyName);
        delete(storyDir);        
        // prepare a new folder for the cloned repository
        File localPath = getLocalWorkFileObj(metaPath);        
        
        // then clone
        String log = "0. Cloning from " + gitRemoteURL + " to " + localPath +"\r\n";
        dlog.info(storyName, log, logger);
        try {
	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
        	Git result = cloneRepo(gitUser, gitPass, localPath);
 	        log = "1. Clonned repository: " + result.getRepository().getDirectory() + " $git clone "+gitRemoteURL+"\r\n";
 	        dlog.info(storyName, log, logger);
	        
	        //////////////////////////////////
	        // Checkout Empty branch to start	        
	        //Git git = Git.open(localPath); //checkout is the folder with .git
	        //System.out.println(git.getRepository().getFullBranch());
	        //CheckoutCommand checkout = git.checkout();
	        //Ref call = checkout.setName("origin/empty").call();
	        result.checkout().setName("origin/empty").call();	
	        result.close();
	        log = "2. Switch to empty branch to start: $git checkout empty\r\n";
	        dlog.info(storyName, log, logger);
	        
	        ////////////////////////////////////////
	        // Create new Story Branch and checkout
	        result.branchCreate().setName(storyName).call();
	        result.close();
	        result.checkout().setName(storyName).call();
	        result.close();
	        log = "3. Create new story branch from empty and checkout new branch: $git checkout -b "+storyName+"\r\n";
	        dlog.info(storyName, log, logger);
	        
	        ////////////////////////////
	        // Pull metadata with ant
	        String sourcePath = metaPath+"/src";
	        // Ensure created directory
	        File storyPath = new File(sourcePath);
	        storyPath.mkdirs();
	        log = "4. Run ant to pull changeset from source "+sanboxName+" org: $ant pullChangeSet\r\n";
	        dlog.info(storyName, log, logger);
	        log=antTaskPull(sourcePath, storyName, sanboxName);	        
	        dlog.info(storyName, log, logger);
	        
	        /////////////////////////////
	        // Add metadata for story and commit local
	        addCommitStory(result, localPath, storyName);
	        log = "5. Add story files to local repo tracking: $git add .\r\n";
	        dlog.info(storyName, log, logger);
	        log = "6. Commit story files to local repo with comments: $git commit -m \""+storyName+" Make it better\"\r\n";
	        dlog.info(storyName, log, logger);
	        
	        ////////////////////////////
	        // Push commit remote
	        pushCommitStory(result, localPath, storyName);
	        log = "7. Push all comimted story files to remote: $git push origin "+storyName+"\r\n";
	        dlog.info(storyName, log, logger);
	        
	        ////////////////////////////
	        // Deploy story to target
	        log = "8. Run ant to deploy story "+storyName+" to target "+targetSBName+" org: $ant deployChangeSet\r\n";
	        dlog.info(storyName, log, logger);
	        log=antTaskDeploy(sourcePath, storyName, targetSBName);	        
	        dlog.info(storyName, log, logger);

	        ///////////////////////////
	        // Upload package.xml to WORK items story record
	        log = "9. Upload package.xml attachement to story work item\r\n";
	        dlog.info(storyName, log, logger);
	        AttachmentUpload.savePackageAttachment(metaPath, storyId, sessionId, serverUrl);
	        
	        //////////////////////////
	        // Chatter story deployment status
	        // Story pushed to QA - default value
	        log = "10. Post status to chatter @mention assigned " + assignedId +" user\r\n";
	        dlog.info(storyName, log, logger);
	        CatterPost.postChatterStatus(chatterPost, storyId, assignedId, sessionId);	        
	        
	        
	        log="*** SUCCESS - DEPLOYMENT PROCESS COMPLETE ***\r\n";
	        dlog.info(storyName, log, logger);
	        
        } catch(Exception e){ // END CLONE operation
        	e.printStackTrace();
        	strlog="\r\n***ERROR***"+e.toString();
        	dlog.info(storyName, strlog, logger);
        }
        return strlog;
        
    }
    
    // Initial commit of Change Set to new git branch
    // NOTE: Only use this 1st time to commit use Update for adding changes to existing branch
    // 1. Clone github repo
    // 2. Checkout create new empty Story branch
    // 3. Pull Change set from target developer org to local branch directory
    // 4. Add and commit new source changes locally
    // 5. Push new Story branch to remote github repo
    public static String initialCommitChangeSet(String m_storyId, 
									    		String m_storyName, 
									    		String m_assignedId,
									    		String m_sandbox, // Source sandbox AIDEV, SKYWAYDEV
									    		String m_target,  // Target Sandbox/s SKYWAYQA, SKYWAYUAT
									    		String m_sessionId,
									    		String m_apiURL, DeployLog dlog) throws IOException, GitAPIException, ConnectionException {

    	final Logger logger = LoggerFactory.getLogger(CloneRemoteRepositoryWithAuthentication.class);

    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();    
    	
    	String gitRemoteURL = env.getGitRemoteURL();
    	String gitUser = env.getGitUser();
    	String gitPass = env.getGitPass();   

    	String sessionId = (m_sessionId != null) ? m_sessionId : SESSION_ID;
    	String storyName = (m_storyName != null) ? m_storyName : "W-003243";
    	String storyId = (m_storyId != null) ? m_storyId : "a1s4C000000HLFmQAO";
    	//String assignedId = (m_assignedId != null) ? m_assignedId : "00561000001Z262AAC";
    	String serverUrl = m_apiURL;
    	String sanboxName = (m_sandbox != null) ? m_sandbox : "comdev";
    	//String targetSBName = (m_target != null) ? m_target : "SKYWAYQA";


    	String strlog = "*** START INITIAL COMMIT DEPLOYMENT Process - "+storyName+" ***\r\n\r\n";
    	dlog.info(storyName, strlog, logger);
    	// Variabels
    	String metaPath = getGitRepoPath(storyName);  
    	// Delete existing folder repository
    	File storyDir = new File( WORKING_DIR + storyName);
    	delete(storyDir);        
    	// prepare a new folder for the cloned repository
    	File localPath = getLocalWorkFileObj(metaPath);        

    	// then clone
    	String log = "0. Cloning from " + gitRemoteURL + " to " + localPath +"\r\n";
    	dlog.info(storyName, log, logger);
    	try {
    		// Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
    		Git result = cloneRepo(gitUser, gitPass, localPath);
    		log = "1. Clonned repository: " + result.getRepository().getDirectory() + " $git clone "+gitRemoteURL+"\r\n";
    		dlog.info(storyName, log, logger);

    		//////////////////////////////////
    		// Checkout Empty branch to start	        
    		//Git git = Git.open(localPath); //checkout is the folder with .git
    		//System.out.println(git.getRepository().getFullBranch());
    		//CheckoutCommand checkout = git.checkout();
    		//Ref call = checkout.setName("origin/empty").call();
    		result.checkout().setName("origin/empty").call();	
    		result.close();
    		log = "2. Switch to empty branch to start: $git checkout empty\r\n";
    		dlog.info(storyName, log, logger);

    		////////////////////////////////////////
    		// Create new Story Branch and checkout
    		result.branchCreate().setName(storyName).call();
    		result.close();
    		result.checkout().setName(storyName).call();
    		result.close();
    		log = "3. Create new story branch from empty and checkout new branch: $git checkout -b "+storyName+"\r\n";
    		dlog.info(storyName, log, logger);

    		////////////////////////////
    		// Pull metadata with ant
    		String sourcePath = metaPath+"/src";
    		// Ensure created directory
    		File storyPath = new File(sourcePath);
    		storyPath.mkdirs();
    		log = "4. Run ant to pull changeset from source "+sanboxName+" org: $ant pullChangeSet\r\n";
    		dlog.info(storyName, log, logger);
    		log=antTaskPull(sourcePath, storyName, sanboxName);	        
    		dlog.info(storyName, log, logger);

    		if(isDeploymentGood(log)){
	    		/////////////////////////////
	    		// Add metadata for story and commit local
	    		addCommitStory(result, localPath, storyName);
	    		log = "5. Add story files to local repo tracking: $git add .\r\n";
	    		dlog.info(storyName, log, logger);
	    		log = "6. Commit story files to local repo with comments: $git commit -m \""+storyName+" Make it better\"\r\n";
	    		dlog.info(storyName, log, logger);
	
	    		////////////////////////////
	    		// Push commit remote
	    		pushCommitStory(result, localPath, storyName);
	    		log = "7. Push all comimted story files to remote: $git push origin "+storyName+"\r\n";
	    		dlog.info(storyName, log, logger);
	
	    		///////////////////////////
	    		// Upload package.xml to WORK items story record
	    		log = "8. Upload package.xml attachement to story work item\r\n";
	    		dlog.info(storyName, log, logger);
	    		AttachmentUpload.savePackageAttachment(metaPath, storyId, sessionId, serverUrl);
	
	    		log="*** SUCCESS - STORY COMMIT PROCESS COMPLETE ***\r\n";
	    		dlog.info(storyName, log, logger);
    		
    		}else{
        		log="*** ERROR - STORY COMMIT PROCESS ERROR ***\r\n";
        		dlog.info(storyName, log, logger);
    			
    		}

    	} catch(Exception e){ // END CLONE operation
    		e.printStackTrace();
    		strlog="\r\n***ERROR***"+e.toString();
    		dlog.info(storyName, strlog, logger);
    	}
    	return strlog;

    }

    // Update commit of Change Set to existing git branch
    // NOTE: Use this to commit changes to existing branch
    // 1. Clone ginthub repo
    // 2. Checkout existing Story branch
    // 3. Pull Change set from target developer org to local branch directory
    // 4. Add and commit new source changes locally
    // 5. Push update to Story branch to remote github repo
    public static String updateCommitChangeSet(String m_storyId, 
    											String m_storyName, 
									    		String m_assignedId,
									    		String m_sandbox, // Source sandbox name AIDEV, SKYWAYDEV
									    		String m_target,  // Target Sandbox/s name SKYWAYQA, SKYWAYUAT
									    		String m_sessionId,
									    		String m_apiURL, DeployLog dlog) throws IOException, GitAPIException, ConnectionException {

    	final Logger logger = LoggerFactory.getLogger(CloneRemoteRepositoryWithAuthentication.class);

    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();    
    	
    	String gitRemoteURL = env.getGitRemoteURL();
    	String gitUser = env.getGitUser();
    	String gitPass = env.getGitPass();   

    	String sessionId = (m_sessionId != null) ? m_sessionId : SESSION_ID;
    	String storyName = (m_storyName != null) ? m_storyName : "W-003243";
    	String storyId = (m_storyId != null) ? m_storyId : "a1s4C000000HLFmQAO";
    	String serverUrl = m_apiURL;
    	String sanboxName = (m_sandbox != null) ? m_sandbox : "comdev";


    	String strlog = "*** START UPDATE COMMIT DEPLOYMENT Process - "+storyName+" ***\r\n\r\n";
    	dlog.info(storyName, strlog, logger);
    	// Variabels
    	String metaPath = getGitRepoPath(storyName);  
    	// Delete existing folder repository
    	File storyDir = new File( WORKING_DIR + storyName);
    	delete(storyDir);        
    	// prepare a new folder for the cloned repository
    	File localPath = getLocalWorkFileObj(metaPath);        

    	// then clone
    	String log = "0. Cloning from " + gitRemoteURL + " to " + localPath +"\r\n";
    	dlog.info(storyName, log, logger);
    	try {
    		// Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
    		Git result = cloneRepo(gitUser, gitPass, localPath);
    		log = "1. Clonned repository: " + result.getRepository().getDirectory() + " $git clone "+gitRemoteURL+"\r\n";
    		dlog.info(storyName, log, logger);

	        ////////////////////////////////////////
	        // Checkout existing Story Branch to local
	        result.checkout().setName("origin/"+storyName).call();
	        result.close();
    		log = "2. Checkout existing story branch: $git checkout "+storyName+"\r\n";
    		dlog.info(storyName, log, logger);

    		////////////////////////////
    		// Pull metadata with ant
    		String sourcePath = metaPath+"/src";
    		// Ensure created directory
    		File storyPath = new File(sourcePath);
    		// Remove all source dir to updates from ChangeSet and pick up deleted items
    		delete(storyPath);
    		// Create src dir new fresh
    		storyPath.mkdirs();
    		log = "3. Run ant to pull changeset from source "+sanboxName+" org: $ant pullChangeSet\r\n";
    		dlog.info(storyName, log, logger);
    		log=antTaskPull(sourcePath, storyName, sanboxName);	        
    		dlog.info(storyName, log, logger);

    		if(isDeploymentGood(log)){
	    		/////////////////////////////
	    		// Add metadata for story and commit local
	    		addCommitStory(result, localPath, storyName);
	    		log = "4. Add story files to local repo tracking: $git add .\r\n";
	    		dlog.info(storyName, log, logger);
	    		log = "5. Commit story files to local repo with comments: $git commit -m \""+storyName+" Make it better\"\r\n";
	    		dlog.info(storyName, log, logger);
	
	    		////////////////////////////
	    		// Push commit remote
	    		pushCommitStory(result, localPath, storyName);
	    		log = "6. Push all comimted story files to remote: $git push origin "+storyName+"\r\n";
	    		dlog.info(storyName, log, logger);
	
	    		///////////////////////////
	    		// Upload package.xml to WORK items story record
	    		log = "7. Upload package.xml attachement to story work item\r\n";
	    		dlog.info(storyName, log, logger);
	    		AttachmentUpload.savePackageAttachment(metaPath, storyId, sessionId, serverUrl);
	
	    		log="*** SUCCESS - STORY UPDATE COMMIT PROCESS COMPLETE ***\r\n";
	    		dlog.info(storyName, log, logger);
    		}else{
        		log="*** ERROR - STORY UPDATE COMMIT PROCESS ERROR ***\r\n";
        		dlog.info(storyName, log, logger);
    			
    		}

    	} catch(Exception e){ // END CLONE operation
    		e.printStackTrace();
    		strlog="\r\n***ERROR***"+e.toString();
    		dlog.info(storyName, strlog, logger);
    	}
    	return strlog;

    }

    public static String deployChangeSet(String m_storyId, 
							    		String m_storyName, 
							    		String m_assignedId,
							    		String m_sandbox, // Source sandbox AIDEV, SKYWAYDEV
							    		String m_target,  // Target Sandbox/s SKYWAYQA, SKYWAYUAT
							    		String m_sessionId,
							    		String m_apiURL, DeployLog dlog) throws IOException, GitAPIException, ConnectionException {

    	final Logger logger = LoggerFactory.getLogger(CloneRemoteRepositoryWithAuthentication.class);

    	// Get settings from system Enviornment
    	DeployEnv sysenv = new DeployEnv();    
    	
    	String gitRemoteURL = sysenv.getGitRemoteURL();
    	String gitUser = sysenv.getGitUser();
    	String gitPass = sysenv.getGitPass();   

    	String sessionId = (m_sessionId != null) ? m_sessionId : SESSION_ID;
    	String storyName = (m_storyName != null) ? m_storyName : "W-003243";
    	String storyId = (m_storyId != null) ? m_storyId : "a1s4C000000HLFmQAO";
    	String assignedId = (m_assignedId != null) ? m_assignedId : "00561000001Z262AAC";
    	String targetSBName = (m_target != null) ? m_target : "SKYWAYQA";

    	// Make chatter post TEXT
    	String chatterPost = "Story "+ storyName + " deployed to "+targetSBName;

    	String strlog = "*** START STORY DEPLOYMENT Process - "+storyName+" ***\r\n\r\n";
    	dlog.info(storyName, strlog, logger);
    	// Variabels
    	String metaPath = getGitRepoPath(storyName);  
    	// Delete existing folder repository
    	File storyDir = new File( WORKING_DIR + storyName);
    	delete(storyDir);        
    	// prepare a new folder for the cloned repository
    	File localPath = getLocalWorkFileObj(metaPath);        

    	// then clone
    	String log = "0. Cloning from " + gitRemoteURL + " to " + localPath +"\r\n";
    	dlog.info(storyName, log, logger);
    	try {
    		// Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
    		Git result = cloneRepo(gitUser, gitPass, localPath);
    		log = "1. Clonned repository: " + result.getRepository().getDirectory() + " $git clone "+gitRemoteURL+"\r\n";
    		dlog.info(storyName, log, logger);

	        ////////////////////////////////////////
	        // Checkout existing Story Branch to local
	        result.checkout().setName("origin/"+storyName).call();
	        result.close();
    		log = "2. Checkout existing story branch: $git checkout -b "+storyName+"\r\n";
    		dlog.info(storyName, log, logger);

    		////////////////////////////
    		// Deploy story to target
    		String sourcePath = metaPath+"/src";
    		log = "3. Run ant to deploy story "+storyName+" to target "+targetSBName+" org: $ant deployChangeSet\r\n";
    		dlog.info(storyName, log, logger);
    		log = antTaskDeploy(sourcePath, storyName, targetSBName);	        
    		dlog.info(storyName, log, logger);

    		if(isDeploymentGood(log)){
	    		//////////////////////////
	    		// Chatter story deployment status
	    		// Story pushed to QA - default value
	    		log = "4. Post status to chatter @mention assigned user\r\n";
	    		dlog.info(storyName, log, logger);
	    		CatterPost.postChatterStatus(chatterPost, storyId, assignedId, sessionId);	        
	
	    		// Update Environment for deploy
	    		log = "5. Update environment for deployment UAT;QA etc.\r\n";
	    		dlog.info(storyName, log, logger);
	        	String env = UpdateWorkItem.updateStory(storyId, sessionId, targetSBName);
	    		log="Updated env - "+env+"\r\n";
	    		dlog.info(storyName, log, logger);

	        	
	    		log="*** SUCCESS - DEPLOYMENT PROCESS COMPLETE ***\r\n";
	    		dlog.info(storyName, log, logger);
	    		

    		} else {
	    		log="*** ERROR - DEPLOYMENT PROCESS FAILED ***\r\n";
	    		dlog.info(storyName, log, logger);   			
    		}

    	} catch(Exception e){ // END CLONE operation
    		e.printStackTrace();
    		strlog="\r\n***ERROR***"+e.toString();
    		dlog.info(storyName, strlog, logger);
    	}
    	return strlog;

    }
    
    public static boolean isDeploymentGood(String lg){
    	boolean rc = false;
    	// look at DEPLOYMENT SUCCEEDED or BUILD SUCCESSFUL
    	String key = "DEPLOYMENT SUCCEEDED";
    	String key1 = "BUILD SUCCESSFUL";
    	
    	if(lg.contains(key) || lg.contains(key1)){
    		rc = true;
    	}
    	
    	return rc;
    }
    public static String updateRepo(String m_storyId, 
									String m_storyName, 
									String m_assignedId,
									String m_sandbox,
									String m_target,
									String m_sessionId,
									String m_apiURL) throws IOException, GitAPIException, ConnectionException {

    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();    
    	
    	String gitRemoteURL = env.getGitRemoteURL();
    	String gitUser = env.getGitUser();
    	String gitPass = env.getGitPass();   
    	
    	String sessionId = (m_sessionId != null) ? m_sessionId : SESSION_ID;
    	String storyName = (m_storyName != null) ? m_storyName : "W-003243";
    	String storyId = (m_storyId != null) ? m_storyId : "a1s4C000000HLFmQAO";
    	String assignedId = (m_assignedId != null) ? m_assignedId : "00561000001Z262AAC";
    	String serverUrl = m_apiURL;
    	String sanboxName = (m_sandbox != null) ? m_sandbox : "comdev";
    	String targetSBName = (m_target != null) ? m_target : "SKYWAYQA";
    	
    	// Make chatter post TEXT
    	String chatterPost = "Story "+ storyName + " deployed to "+targetSBName;

    	
		String strlog = "*** START COMMIT DEPLOYMENT Process - "+storyName+" ***<br/>";
        // Variabels
        String metaPath = getGitRepoPath(storyName);     
        // Delete existing folder repository
        File storyDir = new File( WORKING_DIR + storyName);
        delete(storyDir);
        // prepare a new folder for the cloned repository
        File localPath = getLocalWorkFileObj(metaPath);        
        
        // then clone
        System.out.println("0. Cloning from " + gitRemoteURL + " to " + localPath +"<br/>");
        try {

	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
        	Git result = cloneRepo(gitUser, gitPass, localPath);
 	        System.out.println("1. Clonned repository: " + result.getRepository().getDirectory() + "<br/>");	        
	        strlog += "1. Clonned repository: <i>$git clone "+gitRemoteURL+"</i><br/>";
	                
	        ////////////////////////////////////////
	        // Checkout existing Story Branch to local
	        result.checkout().setName("origin/"+storyName).call();
	        result.close();
	        System.out.println("2. Create and Checkout New story Branch: " + storyName+"<br/>");
	        strlog+="2. Create new story branch from empty and checkout new branch: <i>$git checkout -b "+storyName+"</i><br/><br/>";
	        
	        ////////////////////////////
	        // Pull metadata with ant
	        String sourcePath = metaPath+"/src";
	        System.out.println("<br/>3. Run ant to pull changeset from source "+sanboxName+" org<br/><br/>");
	        strlog+="3. Run ant to pull changeset from source "+sanboxName+" org: <i>$ant pullChangeSet</i><br/>";
	        strlog += antTaskPull(sourcePath, storyName, sanboxName);

	        ////////////////////////////
	        // Deploy story to target
	        System.out.println("<br/>4. Run ant to deploy story to target "+targetSBName+" org<br/><br/>");
	        strlog+="4. Run ant to deploy story "+storyName+" to target "+targetSBName+" org: <i>$ant deployChangeSet</i><br/>";
	        strlog += antTaskDeploy(sourcePath, storyName, targetSBName);	        
	        
	        /////////////////////////////
	        // Add metadata for story and commit local
	        addCommitStory(result, localPath, storyName);
	        System.out.println("5. Add story files to local repo tracking<br/>");
	        System.out.println("6. Commit story files to local repo with comments<br/>");
	        strlog+="5. Add story files to local repo tracking: <i>$git add .</i><br/>";
	        strlog+="6. Commit story files to local repo with comments: <i>$git commit -m \""+storyName+" Make it better\"</i><br/>";
	        
	        ////////////////////////////
	        // Push commit remote
	        try{
	        	pushCommitStory(result, localPath, storyName);
	        	System.out.println("7. Push all comimted story files to remote<br/><br/>");
	        	strlog+="7. Push all comimted story files to remote: <i>$git push origin "+storyName+"</i><br/>";
	        }catch(TransportException te){
	        	System.out.println("7. No new file to push to remote<br/><br/>");
	        	strlog+="7. No new file to push to remote: <i>$git push origin "+storyName+"</i><br/>";	        	
	        }
	        //////////////////////////
	        // Chatter story deployment status
	        // Story pushed to QA - default value
	        System.out.println("<br/>8. Post status to chatter @mention assigned user<br/>");
	        CatterPost.postChatterStatus(chatterPost, storyId, assignedId, sessionId);	        
	        strlog+="8. Post status to chatter @mention assigned user<br/>";
	        
	        ///////////////////////////
	        // Upload package.xml to WORK items story record
	        AttachmentUpload.savePackageAttachment(metaPath, storyId, sessionId, serverUrl);
	        System.out.println("<br/>9. Upload package.xml attachement to story work item<br/>");
	        strlog+="9. Upload package.xml attachement to story work item<br/>";
	        
	        strlog+="*** DEPLOYMENT PROCESS COMPLETE ***<br/>";
        	
        	
        } catch(Exception e){ // END CLONE operation
        	e.printStackTrace();
        	strlog+="***ERROR***"+e.toString();
        }
		
		return strlog;
    }    
    
    public static Git cloneRepo(String gitUser, String gitPass, File localPath) throws InvalidRemoteException, TransportException, GitAPIException{
    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();        	
    	String gitRemoteURL = env.getGitRemoteURL();
    	Git result = Git.cloneRepository()
                .setURI(gitRemoteURL)
                .setDirectory(localPath)
                .setCredentialsProvider( new UsernamePasswordCredentialsProvider( gitUser, gitPass ) )
                .call();
    	result.close();
    	
    	return result;
    }
    
    public static void pushCommitStory(Git git, File localPath, String storyName) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();    
    	
    	String gitUser = env.getGitUser();
    	String gitPass = env.getGitPass();   
    	
    	// Add new file/s and add it to the index
    	//Git git = Git.open(localPath);    	
    	git.push()
    		.add("origin "+storyName)
    		.setCredentialsProvider( new UsernamePasswordCredentialsProvider( gitUser, gitPass ) )
    		.call();
    }
    
    public static void addCommitStory(Git git, File localPath, String storyName) throws IOException, NoFilepatternException, GitAPIException{
    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();        	
    	String baseUserEmail = env.getBaseUserEmail();
    	String gitUser = env.getGitUser();
    	// Add new file/s and add it to the index
    	//Git git = Git.open(localPath); 
    	git.add().addFilepattern(".").call();
    	// Now, we do the commit files with a message
    	// Story commit
    	String msg = storyName+" story initial commit";
    	git.commit().setAuthor(gitUser, baseUserEmail).setMessage(msg).call();  
    
    }
    
    public static String antTaskPull(String storypath, String storyName, String sanboxName) {
    	String log = "";
		File buildFile = new File("build.xml");
		Project p = setAntProperties(storypath, storyName, sanboxName);
		
		DefaultLogger consoleLogger = setAntLogger();
		p.addBuildListener(consoleLogger);

		final DefaultLogger errorLogger = new DefaultLogger();
	    final ByteArrayOutputStream errb = new ByteArrayOutputStream();
	    final PrintStream errp = new PrintStream(errb);
	    errorLogger.setErrorPrintStream(errp);
	    
	    final ByteArrayOutputStream outb = new ByteArrayOutputStream();
	    final PrintStream outp = new PrintStream(outb);
	    errorLogger.setOutputPrintStream(outp);
	    errorLogger.setMessageOutputLevel(Project.MSG_INFO);
	    p.addBuildListener(errorLogger);
		
		
		try{
			p.fireBuildStarted();
		
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			p.addReference("ant.projectHelper", helper);
			helper.parse(p, buildFile);
			//p.executeTarget(p.getDefaultTarget());
			p.executeTarget("pullChangeSet");
		
			p.fireBuildFinished(null);
			
			// Save log
			log += antBuildProblems(new String(outb.toByteArray()),
                    			new String(errb.toByteArray()));
		
		} catch (BuildException e){
			p.fireBuildFinished(e);
			// Save log
			log += antBuildProblems(new String(outb.toByteArray()),
                    			new String(errb.toByteArray()));

		}
		return log;
    }

    public static String antTaskDeploy(String storypath, String storyName, String sanboxName) {
    	String log = "";
    	
		File buildFile = new File("build.xml");
		Project p = setAntProperties(storypath, storyName, sanboxName);
		
		DefaultLogger consoleLogger = setAntLogger();
		p.addBuildListener(consoleLogger);

		final DefaultLogger errorLogger = new DefaultLogger();
	    final ByteArrayOutputStream errb = new ByteArrayOutputStream();
	    final PrintStream errp = new PrintStream(errb);
	    errorLogger.setErrorPrintStream(errp);
	    
	    final ByteArrayOutputStream outb = new ByteArrayOutputStream();
	    final PrintStream outp = new PrintStream(outb);
	    errorLogger.setOutputPrintStream(outp);
	    errorLogger.setMessageOutputLevel(Project.MSG_INFO);
	    p.addBuildListener(errorLogger);

		try{
			p.fireBuildStarted();
		
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			p.addReference("ant.projectHelper", helper);
			helper.parse(p, buildFile);
			//p.executeTarget(p.getDefaultTarget());
			p.executeTarget("deployChangeSet");
		
			p.fireBuildFinished(null);
		
			// Save log
			log += antBuildProblems(new String(outb.toByteArray()),
                    			new String(errb.toByteArray()));

		} catch (BuildException e){
			p.fireBuildFinished(e);
			// Save log
			log += antBuildProblems(new String(outb.toByteArray()),
                    			new String(errb.toByteArray()));
			
		}
		return log;
    }
  
   public static String antBuildProblems(String outPile, String errPile) { 
        String log = "\r\n";
    	final String[] outLines = outPile.split(System.getProperty("line.separator")); 
        final String[] errLines = errPile.split(System.getProperty("line.separator")); 
     
        for (final String line : outLines) { 
        	log += line+"\r\n";
        } 
        for (final String line : errLines) { 
        	log += line+"\r\n";
        } 
        log += "\r\n";
        return log;
    }     

    public static Project setAntProperties(String storypath, String storyName, String sanboxName){
    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();        	

		Project p = new Project();
		//p.setUserProperty("build.properties", buildFile.getAbsolutePath());
		p.setProperty("sf.username", getSourceSandboxUser(sanboxName));
		p.setProperty("sf.password", env.getSfUserPass());
		p.setProperty("sf.maxPoll", "400");
		p.setProperty("sf.serverurl", env.getSfServerUrl());
		p.setProperty("aa.story", storyName);
		p.setProperty("sf.retrieveTarget", storypath);
		return p;
    }
    
    public static DefaultLogger setAntLogger(){
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		return consoleLogger;
    }
    
    // Pass Sandbobox name prefix such as AIDEV, ASTRODEV, CODEYDEV, HOOTDEV
    // to get source user for pull story changesets
    public static String getSourceSandboxUser(String sbn){
    	// Get settings from system Enviornment
    	DeployEnv env = new DeployEnv();        	
    	String baseUserEmail = env.getBaseUserEmail();
    	String str = sbn.toLowerCase();
    	System.out.println("Pull USER: "+baseUserEmail+'.'+str);
    	
    	return baseUserEmail+'.'+str;
    }
    
    public static String getGitRepoPath(String storyName){
    	// Get settings from system Environment
    	DeployEnv env = new DeployEnv();        	
    	String metaPath = WORKING_DIR + storyName + "/" + env.getGitRepoName();
    	return metaPath;
    }
    
    public static File getLocalWorkFileObj(String metaPath) throws IOException{
        File localPath = new File(metaPath);
        if (localPath.exists()) {
        	if(!localPath.delete()) {
        		throw new IOException("Could not delete temporary file " + localPath + "\r\n");
        	}
        }
        localPath.mkdirs();
    	return localPath;
    }

    public static void delete(File file)
        	throws IOException{

        	if(file.isDirectory()){

        		//directory is empty, then delete it
        		if(file.list().length==0){

        		   file.delete();
        		   System.out.println("Directory is deleted : "
                                                     + file.getAbsolutePath());

        		}else{

        		   //list all the directory contents
            	   String files[] = file.list();

            	   for (String temp : files) {
            	      //construct the file structure
            	      File fileDelete = new File(file, temp);

            	      //recursive delete
            	     delete(fileDelete);
            	   }

            	   //check the directory again, if empty then delete it
            	   if(file.list().length==0){
               	     file.delete();
            	     System.out.println("Directory is deleted : "
                                                      + file.getAbsolutePath());
            	   }
        		}

        	}else{
        		//if file, then delete it
        		file.delete();
        		System.out.println("File is deleted : " + file.getAbsolutePath());
        	}
        }
}
