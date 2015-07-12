package com.customtime.data.conversion.domain.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.customtime.data.conversion.plugin.monitor.PluginMonitor;

public class DefaultPluginMonitor implements PluginMonitor {

	private int successLine = 0;
	private List<String> failines = new ArrayList<String>();
	private Map<String,Long> successLineMap = new HashMap<String,Long>();
	private Map<String,List<String>> failinesMap = new HashMap<String,List<String>>();
	
	public int getSuccessLine() {
		return successLine;
	}

	public void successLint() {
		successLine();
	}
	
	public void successLine() {
		successLine++;
	}

	public void failureLine(String str) {
		failines.add(str);
	}

	public List<String> getFailines() {
		return failines;
	}

	public void successLine(String key){
		Long tLong = successLineMap.get(key);
		if(tLong==null){
			tLong = 0L;
		}
		tLong++;
		successLineMap.put(key, tLong);
	}
	
	public Long getSuccessLine(String key){
		Long tLong = successLineMap.get(key);
		if(tLong==null){
			return 0L;
		}
		return tLong;
	}
	
	public Map<String,Long> getAllSuccessLine(){
		return successLineMap;
	}
	
	
	public void failureLine(String key, String str){
		List<String> tList = failinesMap.get(key);
		if(tList==null){
			tList = new ArrayList<String>();
		}
		tList.add(str);
		failinesMap.put(key, tList);
	}
	
	public List<String> getFailines(String key){
		return failinesMap.get(key);
	}
	public Map<String,List<String>> getAllFailines(){
		return failinesMap;
	}
}
