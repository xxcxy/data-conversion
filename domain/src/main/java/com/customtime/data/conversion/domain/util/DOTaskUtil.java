package com.customtime.data.conversion.domain.util;

import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.config.EngineConfig;
import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.domain.controller.Controller;
import com.customtime.data.conversion.domain.plan.ProcessPlan;
import com.customtime.data.conversion.plugin.exception.PluginConfigException;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;

public class DOTaskUtil {

	public static Controller getController(Command command){
		EngineConfig eConfig = ResourceContext.getEngineConfig();
		ProcessPlan processPlan = eConfig.getProcessPlan();
		PluginMonitor pm = eConfig.getPluginMonitor();
		PluginConfig pc = eConfig.getPluginConfig();
		try {
			pc.setCommandProps(command.getCommandProps());
			pc.setProcessPlanPath(command.getProcessPlanPath());
			processPlan.setPluginConfig(pc);
			processPlan.setPluginMonitor(pm);
			if(processPlan != null)
				return eConfig.getController().setRuleRunner(eConfig.getRuleRunner()).setPlan(processPlan);
		} catch (PluginConfigException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void doTask(String command){
		Command comm = ResourceContext.getEngineConfig().getCommand();
		if(comm.init(command)){
			Controller ct = getController(comm);
			if(ct!=null)
				new Thread(ct).start();
		}
	}
}
