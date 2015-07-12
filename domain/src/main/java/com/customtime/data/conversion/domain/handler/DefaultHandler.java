package com.customtime.data.conversion.domain.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.recode.Recode;

public class DefaultHandler implements TerminatHandler {

	private final static Log logger=LogFactory.getLog(DefaultHandler.class);
	public Recode process(Recode rd) {
		logger.info("deal the recode!");
		return rd;
	}

}
