package com.customtime.data.conversion.domain.context;

import java.util.List;

import com.customtime.data.conversion.plugin.context.PluginContextIntance;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.pool.AbstractPool;

public class PluginContextImpl implements PluginContextIntance{

	public AbstractPool getPool() {
		return ResourceContext.getPool();
	}

	public List<PluginMonitor> getPluginMonitor() {
		return ResourceContext.getPluginConfig().getPluginMonitor();
	}
	
}
