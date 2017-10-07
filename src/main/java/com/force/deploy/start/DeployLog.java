package com.force.deploy.start;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;

public class DeployLog {

	private HashMap<String,ArrayList<LogObj>> hmLogData = new HashMap<String,ArrayList<LogObj>>();

	public void info(String key, String msg, Logger logger){
        //System.out.println(msg);
        logger.info(msg);
        this.addLogData(key, msg);
	}
	
    public void addLogData(String key, String msg){
       	if (hmLogData != null){
       		ArrayList<LogObj> ld = hmLogData.get(key);
       		if (ld != null){
       			LogObj obj = new LogObj();
       			obj.setMessage(msg);
       			ld.add(obj);
       			hmLogData.put(key, ld);
       		}else{
       			ArrayList<LogObj> temp = new ArrayList<LogObj>();
       			LogObj obj = new LogObj();
       			obj.setMessage(msg);
       			temp.add(obj);
       			hmLogData.put(key, temp);
       		}
       	}    	
    }
    
    public String getLogData(String key){
    	String str = "";
       	if (hmLogData != null && hmLogData.size() > 0 && hmLogData.containsKey(key)){
       		ArrayList<LogObj> ld = hmLogData.get(key);
       		//int count = 0;
       		for (LogObj s : ld){
       			if (!s.getIsLogged()){
       				//if(count > 0){str += "\\n";}
       				str += s.getMessage()+"\r\n";
       				//count++;
       				// Set the value TRUE not to pick up next time
       				s.setIsLogged(true);
       			}
       		} 
       		// test log if value was changed
       		ld = hmLogData.get(key);
       		for (LogObj s : ld){
       			if (s.getIsLogged()){
       				System.out.println("### LOG USED TRUE: "+s.getMessage());
       			}else{
       				System.out.println("### LOG USED FALSE: "+s.getMessage());
       			}
       		}
    	}
    	
    	return str;
    }

    public String getLogJsonData(String key){
    	String str = "{[";
       	if (hmLogData != null && hmLogData.size() > 0 && hmLogData.containsKey(key)){
       		ArrayList<LogObj> ld = hmLogData.get(key);
       		int count = 0;
       		for (LogObj s : ld){
       			if (!s.getIsLogged()){
       				if(count > 0){str += ",";}
       				str += "{\"msg\" : \""+s.getMessage()+"\"}";
       				count++;
       				// Set the value TRUE not to pick up next time
       				s.setIsLogged(true);       				
       			}
       		}
    		
    	}
    	
    	return str + "]}";
    }

    public void clearLog(int cnt, String key){
    	ArrayList<LogObj> ld = hmLogData.get(key);
    	for(int i=cnt; i >= 0; i--){
    		ld.remove(i);
    	}
    }
    // Call this method to remove all logs for Story deployment after success
    public void clearAllLog(String key){
    	ArrayList<LogObj> ld = hmLogData.get(key);
    	if (ld !=null && ld.size()>0){
    		ld.clear();
    	}
    }
    
}
