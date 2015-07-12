package com.customtime.data.conversion.domain.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.customtime.data.conversion.domain.acceptor.Acceptor;
import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.domain.controller.Controller;
import com.customtime.data.conversion.domain.plan.ProcessPlan;
import com.customtime.data.conversion.domain.ruler.TerminatExecutable;
import com.customtime.data.conversion.domain.ruler.TerminatRuleRunner;
import com.customtime.data.conversion.domain.temporary.Temporary;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;

public class EngineConfig {

	private Acceptor acceptor;
	private Controller controller;
	private TerminatRuleRunner ruleRunner;
	private RecodesKeeper recodesKeeper;
	private Command command;
	private ProcessPlan processPlan;
	private Recode recode;
	private PluginConfig pluginConfig;
	private PluginMonitor pluginMonitor;
	private TerminatExecutable terminatExecutable;
	private Temporary temporary;
	private Map<Class<?>,Map<String,String>> properties;
	private Map<String,String> tasks;
	
	public Map<String, String> getTasks() {
		return tasks;
	}

	public void setTasks(Map<String, String> tasks) {
		this.tasks = tasks;
	}

	public void setProperties(Map<Class<?>,Map<String,String>> properties){
		this.properties = properties;
	}
	
	public Acceptor getSingleAcceptor() {
		return acceptor;
	}
	public void setAcceptor(Acceptor acceptor) {
		this.acceptor = acceptor;
	}
	public Controller getSingleController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	public TerminatRuleRunner getSingleRuleRunner() {
		return ruleRunner;
	}
	public void setRuleRunner(TerminatRuleRunner ruleRunner) {
		this.ruleRunner = ruleRunner;
	}
	public RecodesKeeper getSingleRecodesKeeper() {
		return recodesKeeper;
	}
	public void setRecodesKeeper(RecodesKeeper recodesKeeper) {
		this.recodesKeeper = recodesKeeper;
	}
	public Command getSingleCommand() {
		return command;
	}
	public void setCommand(Command command) {
		this.command = command;
	}
	public ProcessPlan getSingleProcessPlan() {
		return processPlan;
	}
	public void setProcessPlan(ProcessPlan processPlan) {
		this.processPlan = processPlan;
	}
	public Recode getSingleRecode() {
		return recode;
	}
	public void setRecode(Recode recode) {
		this.recode = recode;
	}
	public PluginConfig getSinglePluginConfig() {
		return pluginConfig;
	}
	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}
	public PluginMonitor getSinglePluginMonitor() {
		return pluginMonitor;
	}
	public void setPluginMonitor(PluginMonitor pluginMonitor) {
		this.pluginMonitor = pluginMonitor;
	}
	
	public TerminatExecutable getSingleTerminatExecutable() {
		return terminatExecutable;
	}
	public void setTerminatExecutable(TerminatExecutable terminatExecutable) {
		this.terminatExecutable = terminatExecutable;
	}
	public Temporary getSingleTemporary() {
		return temporary;
	}
	public void setTemporary(Temporary temporary) {
		this.temporary = temporary;
	}
	public Acceptor getAcceptor() {
		try {
			return getInstance(acceptor.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Controller getController() {
		try {
			return getInstance(controller.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public TerminatRuleRunner getRuleRunner() {
		try {
			return getInstance(ruleRunner.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public RecodesKeeper getRecodesKeeper() {
		try {
			return getInstance(recodesKeeper.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Command getCommand() {
		try {
			return getInstance(command.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public ProcessPlan getProcessPlan() {
		try {
			return getInstance(processPlan.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Recode getRecode() {
		try {
			return getInstance(recode.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public PluginConfig getPluginConfig() {
		try {
			return getInstance(pluginConfig.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public PluginMonitor getPluginMonitor() {
		try {
			return getInstance(pluginMonitor.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public TerminatExecutable getTerminatExecutable() {
		try {
			return getInstance(terminatExecutable.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Temporary getTemporary() {
		try {
			return getInstance(temporary.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	public List<Command> getTaskCommand(){
		List<Command> commands = new ArrayList<Command>();
		for(String comm:tasks.keySet()){
			Command command = getCommand();
			if(command.init(comm))
				commands.add(command);
		}
		return commands;
	}
	public <T> T getInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException{
		T t = clazz.newInstance();
		Map<String,String> props = properties.get(clazz);
		return ResourceContext.setProperties(clazz,props,t);
	}
}
