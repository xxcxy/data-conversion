package com.customtime.data.conversion.domain.controller;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.domain.plan.ProcessPlan;
import com.customtime.data.conversion.domain.ruler.TerminatExecutable;
import com.customtime.data.conversion.domain.ruler.TerminatRuleRunner;
import com.customtime.data.conversion.plugin.addition.TerminatRunable;

public class DefaultController implements Controller{
	
	private ProcessPlan processPlan;
	private TerminatRuleRunner terminatRuleRunner;
	

	public Controller setPlan(ProcessPlan pp) {
		this.processPlan = pp;
		return this;
	}

	public Controller setRuleRunner(TerminatRuleRunner trr) {
		this.terminatRuleRunner = trr;
		return this;
	}

	
	public void run() {
		List<TerminatExecutable> tr = processPlan.getReaders();
		List<List<TerminatExecutable>> tws = processPlan.getWriters();
		List<TerminatRunable> tpr = processPlan.getAllPluginRun();
		PluginConfig pc = processPlan.getPluginConfig();
		ResourceContext.setPluginConfig(pc);
		ResourceContext.setPlanAttr();
		if(tr!=null)
			terminatRuleRunner.addInExecutable(tr);
		if(tws!=null)
			for(List<TerminatExecutable> tw:tws)
				terminatRuleRunner.addOutExecutable(tw);
		terminatRuleRunner.setFinishCallbakMonitors(pc.getFinishCallBakcMonit());
		terminatRuleRunner.setPostCallbakMonitors(pc.getPostMethod());
		terminatRuleRunner.setPreCallbakMonitors(pc.getPreMethod());
		terminatRuleRunner.execute();
		ResourceContext.removeThreadObject();
		if(tpr!=null){
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 3, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(30));
			for(TerminatRunable tprr:tpr)
				threadPoolExecutor.execute(tprr);
		}
	}

}
