package com.customtime.data.conversion.domain.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.annotaion.CallMonitor;
import com.customtime.data.conversion.plugin.annotaion.CallbackType;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.util.MonitorUtil;

public class DefaultWriterHandler implements TerminatHandler {
	private final static Log logger=LogFactory.getLog(DefaultWriterHandler.class);
	@TMProperty
	private String handleName;
	public Recode process(Recode rd) {
		logger.debug(handleName);
		return rd;
	}

	@CallMonitor(CallbackType.FINISHEXEC)
	public void doMonitor(MonitorUtil mu){
		logger.info(mu.getSuccessLine());
	}
}
