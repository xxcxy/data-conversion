package com.customtime.data.conversion.domain.acceptor.command;

import java.io.Serializable;
import java.util.Map;

public interface Command extends Serializable{

	public String getProcessPlanPath();
	public Map<String,String> getCommandProps();
	public boolean init(String commandString);
	public String getTimeSchedule();
}
