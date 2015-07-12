package com.customtime.data.conversion.plugin.writer;

import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;

public interface TerminatWriter extends TerminatRuleRunnable{
	
	public void writing(RecodesKeeper rk);
	

}
