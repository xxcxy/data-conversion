package com.customtime.data.conversion.domain.keeper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.domain.handler.TerminatHandler;
import com.customtime.data.conversion.domain.temporary.Temporary;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;

public class DefaultRKeeper implements RecodesKeeper {
	
	@SuppressWarnings("unused")
	private final static Log logger=LogFactory.getLog(DefaultRKeeper.class);
	private Map<TerminatRuleRunnable,Temporary> temporarys;
	
	public RecodesKeeper init(List<TerminatRuleRunnable> trs){
		temporarys = new HashMap<TerminatRuleRunnable,Temporary>();
		int i=0;
		if(trs==null)
			return this;
		for(TerminatRuleRunnable tr:trs){
			Temporary temporary = ResourceContext.getEngineConfig().getTemporary();
			temporary.init("id"+i, 10000, 0, 0);
			temporary.setMakeObject(ResourceContext.getEngineConfig().getSingleRecode());
			temporarys.put(tr,temporary);
			i++;
		}
		
		return this;
	}

	public RecodesKeeper addWriteRunnable(TerminatRuleRunnable trr){
		Temporary temporary = ResourceContext.getEngineConfig().getTemporary();
		temporary.init(temporary.hashCode()+"", 10000, 0, 0);
		temporary.setMakeObject(ResourceContext.getEngineConfig().getSingleRecode());
		temporarys.put(trr,temporary);
		return this;
	}
	
	public void keeping(Recode rd,TerminatRuleRunnable tr) {
		PluginConfig pc = ResourceContext.getPluginConfig();
		if(pc!=null&&pc.getReadHandler(tr)!=null){
			for(TerminatHandler th:pc.getReadHandler(tr)){
				if(rd.getBlockNum()>0){
					rd = th.process(rd);
				}
			}
		}
		if(rd!=null&&rd.getBlockNum()>0){
			for(Temporary temporary:temporarys.values())
				temporary.push(rd);
		}
	}

	public Recode arising(TerminatRuleRunnable tr) {
		Recode rd = temporarys.get(tr).pull();
		PluginConfig pc = ResourceContext.getPluginConfig();
		if(rd!=null&&pc!=null&&pc.getWriteHandler(tr)!=null){
			for(TerminatHandler th:pc.getWriteHandler(tr))
				rd = th.process(rd);
		}
		return rd;
	}

	public Recode newRecode() {
		return ResourceContext.getEngineConfig().getRecode();
	}

	public RecodesKeeper keepClose() {
		for(Temporary temporary:temporarys.values())
			temporary.setPushClosed();
		return this;
	}

}
