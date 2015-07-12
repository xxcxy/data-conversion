package com.customtime.data.conversion.domain.ruler;

import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;

public interface TerminatExecutable extends Runnable,Cloneable{

	public TerminatExecutable execute();
	public TerminatExecutable setRuler(TerminatRunRuler trr);
	public TerminatExecutable setRuleRunnable(TerminatRuleRunnable terminatRuleRunnable);
	public TerminatExecutable setRecodesKeeper(RecodesKeeper rk);
	public TerminatExecutable setClassLoader(ClassLoader cl);
	public ClassLoader getClassLoader();
	public TerminatExecutable destory();
	public Object clone();
	
}
