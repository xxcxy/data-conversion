package com.customtime.data.conversion.domain.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShutDownWork extends Thread {
	private static final Log logger = LogFactory.getLog(ShutDownWork.class);
	private String fileName;
	public ShutDownWork(String fileName){
		this.fileName=fileName;
	}

	public void run(){
		File file = new File(fileName);
		logger.info(file.delete());
	}
}
