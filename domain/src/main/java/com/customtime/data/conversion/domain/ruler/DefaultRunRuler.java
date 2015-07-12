package com.customtime.data.conversion.domain.ruler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;

public class DefaultRunRuler implements TerminatRunRuler {
	private static Log logger = LogFactory.getLog(DefaultRunRuler.class);
	
	@TMProperty
	private String rulerName;

	public boolean isReady() {
		logger.debug("ruler name:"+rulerName);
		return true;
	}

	public boolean isEnd() {
		return true;
	}

}
