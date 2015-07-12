package com.customtime.data.conversion.domain.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.customtime.data.conversion.domain.handler.TerminatHandler;
import com.customtime.data.conversion.domain.ruler.TerminatExecutable;
import com.customtime.data.conversion.plugin.addition.TerminatRunable;
import com.customtime.data.conversion.plugin.exception.PluginConfigException;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;

public interface PluginConfig {
	public void setProcessPlanPath(String path) throws PluginConfigException;
	public List<TerminatExecutable> getReaders();
	public List<List<TerminatExecutable>> getWriters();
	public Map<TerminatExecutable,List<TerminatHandler>> getReadHandler();
	public Map<TerminatExecutable,List<TerminatHandler>> getWriteHandler();
	public List<TerminatHandler> getReadHandler(TerminatExecutable tr);
	public List<TerminatHandler> getWriteHandler(TerminatExecutable tw);
	public List<TerminatHandler> getReadHandler(TerminatRuleRunnable tr);
	public List<TerminatHandler> getWriteHandler(TerminatRuleRunnable tw);
	public TerminatRunable getPluginThread(String key);
	public List<TerminatRunable> getAllPluginThread();
	public RecodesKeeper getRecodesKeeper() ;
	public void setRecodesKeeper(RecodesKeeper rk);
	public List<PluginMonitor> getPluginMonitor();
	public Map<Object,List<Method>> getFinishCallBakcMonit();
	public Map<Object, List<Method>> getPostMethod();
	public Map<Object, List<Method>> getPreMethod();
	public void setCommandProps(Map<String,String> commandProps);
	public ClassLoader getClassLoader(Object obj);
}
