package com.customtime.data.conversion.domain.ruler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface TerminatRuleRunner {

	public TerminatRuleRunner addInExecutable(TerminatExecutable trr);
	public TerminatRuleRunner addInExecutable(List<TerminatExecutable> trrs);
	public TerminatRuleRunner addOutExecutable(TerminatExecutable trr);
	public TerminatRuleRunner addOutExecutable(List<TerminatExecutable> trrs);
	public TerminatRuleRunner execute();
	public TerminatRuleRunner setFinishCallbakMonitors(Map<Object,List<Method>> finishCallbakMonitors);
	public TerminatRuleRunner setPostCallbakMonitors(Map<Object,List<Method>> postCallbakMonitors);
	public TerminatRuleRunner setPreCallbakMonitors(Map<Object,List<Method>> preCallbakMonitors);
	
}
