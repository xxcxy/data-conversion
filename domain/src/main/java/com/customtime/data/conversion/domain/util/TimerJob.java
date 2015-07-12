package com.customtime.data.conversion.domain.util;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.controller.Controller;

public class TimerJob implements Job{

	public static final String COMMAND_KEY = "command_key";
	public static final String JOB_NAME = "jobName";
	public static final String CRONSCHEDULE = "cronSchedule";
	
	public void execute(JobExecutionContext jobexecutioncontext)
			throws JobExecutionException {
		Object obj = jobexecutioncontext.getJobDetail().getJobDataMap().get(COMMAND_KEY);
		if(obj instanceof Command){
			Command command = (Command)obj;
			Controller ct = DOTaskUtil.getController(command);
			if(ct!=null)
				new Thread(ct).start();
		}
	}

}
