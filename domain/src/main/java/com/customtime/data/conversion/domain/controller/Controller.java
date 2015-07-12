package com.customtime.data.conversion.domain.controller;

import com.customtime.data.conversion.domain.plan.ProcessPlan;
import com.customtime.data.conversion.domain.ruler.TerminatRuleRunner;

public interface Controller extends Runnable{

	public Controller setPlan(ProcessPlan pp);
	public Controller setRuleRunner(TerminatRuleRunner trr);
	
}
