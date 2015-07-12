package com.customtime.data.conversion.plugin.monitor;

import java.util.List;
import java.util.Map;

public interface PluginMonitor {

	public int getSuccessLine();
	public void successLint();
	public void successLine();
	public void failureLine(String str);
	public List<String> getFailines();
	
	public Long getSuccessLine(String key);
	public Map<String,Long> getAllSuccessLine();
	public void successLine(String key);
	public void failureLine(String key, String str);
	public List<String> getFailines(String key);
	public Map<String,List<String>> getAllFailines();
	
}
