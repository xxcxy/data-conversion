package com.customtime.data.conversion.plugin.reader;

import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;

public interface TerminatReader extends TerminatRuleRunnable{

	public void reading(RecodesKeeper rk);
	
}
