package com.customtime.data.conversion.plugin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.customtime.data.conversion.plugin.context.PluginContext;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;


public class MonitorUtil {

	private List<PluginMonitor> pluginMonitors;
	public MonitorUtil setPluginMonitors(){
		pluginMonitors = PluginContext.getPluginMonitor();
		return this;
	}
	
	public int getSuccessLine(){
		int successLine = 0;
		for(PluginMonitor pm:pluginMonitors){
			successLine += pm.getSuccessLine();
		}
		return successLine;
	}
	
	public List<String> getFailines(){
		List<String> rl = new ArrayList<String>();
		for(PluginMonitor pm:pluginMonitors){
			if(pm.getFailines()!=null){
				rl.addAll(pm.getFailines());
			}
		}
		return rl;
	}
	
	public Long getSuccessLine(String key){
		Long ttLong = 0L;
		for(PluginMonitor pm:pluginMonitors){
			Long tLong = pm.getSuccessLine(key);
			if(tLong!=null){
				ttLong += tLong;
			}
		}
		return ttLong;
	}
	
	public Map<String,Long> getAllSuccessLine(){
		Map<String,Long> map = new HashMap<String,Long>();
		for(PluginMonitor pm:pluginMonitors){
			Map<String,Long> tMap = pm.getAllSuccessLine();
			if(tMap==null) continue;
			Iterator<Entry<String, Long>> iter = tMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Long> entry = (Map.Entry<String, Long>) iter.next();
				String key = entry.getKey();
				Long value = entry.getValue();
				if(value == null){
					value = 0L;
				}
				Long tLong;
				if((tLong = map.get(key))!=null){
					map.put(key, tLong + value);
				}else{
					map.put(key, value);
				}
			}
		}
		return map;
	}
	
	public List<String> getFailines(String key){
		List<String> tList = new ArrayList<String>();
		for(PluginMonitor pm:pluginMonitors){
			List<String> ttList = pm.getFailines(key);
			if(ttList!=null){
				tList.addAll(ttList);
			}
		}
		return tList;
	}
	
	public Map<String,List<String>> getAllFailines(){
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		for(PluginMonitor pm:pluginMonitors){
			Map<String,List<String>> tMap = pm.getAllFailines();
			if(tMap==null) continue;
			Iterator<Entry<String, List<String>>> iter = tMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>) iter.next();
				String key = entry.getKey();
				List<String> value = entry.getValue();
				
				List<String> tList;
				if((tList = map.get(key))!=null&&tList.size()>0&&value!=null&&value.size()>0){
					List<String> ttList = new ArrayList<String>();
					ttList.addAll(tList);
					if(value!=null){
						ttList.addAll(value);
					}
					map.put(key, ttList);
				}else if(value!=null&&value.size()>0){
					map.put(key, value);
				}
			}
		}
		return map;
	}
}
