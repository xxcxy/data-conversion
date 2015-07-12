package com.customtime.data.conversion.plugin.context;

import java.util.List;

import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.pool.AbstractPool;


public class PluginContext {
	
	private static PluginContextIntance instance;
	
	public final static AbstractPool getPool(){
		return instance.getPool();
	}
	
	public final static List<PluginMonitor> getPluginMonitor(){
		return instance.getPluginMonitor();
	}
	
	public final static void setInstance(PluginContextIntance instanceParam){
		instance = instanceParam;
	}
}
