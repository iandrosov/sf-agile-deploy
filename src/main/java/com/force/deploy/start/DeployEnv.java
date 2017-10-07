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
