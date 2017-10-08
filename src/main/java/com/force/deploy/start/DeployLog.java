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
