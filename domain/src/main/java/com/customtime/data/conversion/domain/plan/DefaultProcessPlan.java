package com.customtime.data.conversion.domain.plan;

import java.util.ArrayList;
import java.util.List;

import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.ruler.TerminatExecutable;
import com.customtime.data.conversion.plugin.addition.TerminatRunable;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;

public class DefaultProcessPlan implements ProcessPlan {
	
	private PluginConfig pluginConfig;
	private PluginMonitor pluginMonitor;

	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}

	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

	public PluginMonitor getPluginMonitor() {
		return pluginMonitor;
	}

	public void setPluginMonitor(PluginMonitor pluginMonitor) {
		this.pluginMonitor = pluginMonitor;
	}

	public TerminatExecutable getReader() {
		List<TerminatExecutable> readers = pluginConfig.getReaders();
		if(readers!=null && readers.size()>0)
			return readers.get(0);
		return null;
	}

	public List<TerminatExecutable> getWriter() {
		List<TerminatExecutable> te = new ArrayList<TerminatExecutable>();
		List<List<TerminatExecutable>> writers = pluginConfig.getWriters();
		if(writers!=null && writers.size()>0)
			for(List<TerminatExecutable> writerss:writers)
				te.add(writerss.get(0));
		return te;
	}

	public List<TerminatExecutable> getReaders() {
		return pluginConfig.getReaders();
	}

	public List<List<TerminatExecutable>> getWriters() {
		return pluginConfig.getWriters();
	}

	public TerminatRunable getPluginRun(String key) {
		return pluginConfig.getPluginThread(key);
	}

	public List<TerminatRunable> getAllPluginRun() {
		return pluginConfig.getAllPluginThread();
	}
}
