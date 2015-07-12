package com.customtime.data.conversion.domain.acceptor.command;

import java.util.HashMap;
import java.util.Map;

import com.customtime.data.conversion.domain.util.TimerJob;

public class DefaultCommand implements Command {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String proPlanPath;
	private Map<String,String> commandProps;
	
	public void setProcessPlanPath(String proPlanPath){
		this.proPlanPath=proPlanPath;
	}

	public String getProcessPlanPath() {
		return proPlanPath;
	}
	
	public Map<String, String> getCommandProps() {
		return commandProps;
	}

	public boolean init(String commandString) {
		String[] params = commandString.split("\\s");
		String commandType = params[0];
		commandProps = new HashMap<String,String>();
		for(int i=1,len=params.length;i<len;i++){
			int index = params[i].indexOf('=');
			if(index>0){
				commandProps.put(params[i].substring(0,index), params[i].substring(index+1));
			}
		}
		if("dealPlan".equals(commandType)){
			proPlanPath = commandProps.get("proPlanPath");
			if(proPlanPath==null||"".equals(proPlanPath))
				return false;
		}else{
			return false;
		}
		return true;
	}

	public String getTimeSchedule() {
		String timeSchedule = commandProps.get(TimerJob.CRONSCHEDULE);
		if(timeSchedule!=null && !timeSchedule.equals("")){
			return timeSchedule.replace(",", " ");
		}
		return null;
	}


}
