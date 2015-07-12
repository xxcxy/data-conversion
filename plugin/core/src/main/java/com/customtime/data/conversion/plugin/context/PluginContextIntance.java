package com.customtime.data.conversion.plugin.context;

import java.util.List;

import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.pool.AbstractPool;

public interface PluginContextIntance {
	
	AbstractPool getPool();
	
	List<PluginMonitor> getPluginMonitor();
	
}
