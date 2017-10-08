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

package com.force.deploy.start;

import java.util.Map;

public class DeployEnv {

	public String getGitUser() {
		return gitUser;
	}

	public String getGitPass() {
		return gitPass;
	}

	public String getGitRemoteURL() {
		return gitRemoteURL;
	}

	public String getGitRepoName() {
		return gitRepoName;
	}

	public String getSfServerUrl() {
		return sfServerUrl;
	}

	public String getBaseUserEmail() {
		return baseUserEmail;
	}

	public String getSfUserPass() {
		return sfUserPass;
	}

	private String gitUser;
	private String gitPass;
	private String gitRemoteURL; // GIT URL
	private String gitRepoName; // GIT repository name
	private String sfServerUrl; // Target server url test.salesforce.com
	private String baseUserEmail; // User name as email
	private String sfUserPass; // Gneeric password
	private String sfPackageHost; // Salesforce domain where Agile Accelerator installed to send Chatter updates
	
	public DeployEnv(){
        Map<String, String> env = System.getenv();
        this.gitUser = env.get("GITUSER");
        this.gitPass = env.get("GITPASS");
        this.gitRemoteURL = env.get("GITREMOTEURL");
        this.gitRepoName = env.get("GITREPONAME");
        this.baseUserEmail = env.get("BASEUSEREMAIL");
        this.sfServerUrl = env.get("SFSERVERURL");
        this.sfUserPass = env.get("SFPASS");
        this.sfPackageHost = env.get("SFPACKAGEHOST");
	}

	public String getSfPackageHost() {
		return sfPackageHost;
	}
	public String printEnv(){
		String s = "=== ENV ===\n";
		s+="GITUSER: "+this.gitUser+"\n";
		s+="GITPASS: "+this.gitPass+"\n";
		s+="GITREMOTEURL: "+this.gitRemoteURL+"\n";
		s+="GITREPONAME: "+this.gitRepoName+"\n";
		s+="BASEUSEREMAIL: "+this.baseUserEmail+"\n";
		s+="SFSERVERURL: "+this.sfServerUrl+"\n";
		s+="SFPASS: "+this.sfUserPass+"\n";
		s+="SFPACKAGEHOST: "+this.sfPackageHost+"\n";
		System.out.print(s);
		return s;
	}
	
	
}
