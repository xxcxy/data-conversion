package com.customtime.data.conversion.domain.plan;

import java.util.List;

import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.ruler.TerminatExecutable;
import com.customtime.data.conversion.plugin.addition.TerminatRunable;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;

public interface ProcessPlan {

	public TerminatExecutable getReader();
	public List<TerminatExecutable> getWriter();
	public List<TerminatExecutable> getReaders();
	public List<List<TerminatExecutable>> getWriters();
	public PluginConfig getPluginConfig();
	public void setPluginConfig(PluginConfig pluginConfig);
	public PluginMonitor getPluginMonitor() ;
	public void setPluginMonitor(PluginMonitor pluginMonitor) ;
	public TerminatRunable getPluginRun(String key);
	public List<TerminatRunable> getAllPluginRun();
}
