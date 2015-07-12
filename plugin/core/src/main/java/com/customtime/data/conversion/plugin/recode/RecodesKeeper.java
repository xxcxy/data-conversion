package com.customtime.data.conversion.plugin.recode;

import java.util.List;

import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;


public interface RecodesKeeper {

	public RecodesKeeper  init(List<TerminatRuleRunnable> trs);
	public void keeping(Recode rd,TerminatRuleRunnable tr);
	public Recode arising(TerminatRuleRunnable tr);
	public Recode newRecode();
	public RecodesKeeper keepClose();
	public RecodesKeeper addWriteRunnable(TerminatRuleRunnable trr);
	
}
