package com.customtime.data.conversion.domain.acceptor;

import java.util.ArrayList;
import java.util.List;

import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.acceptor.command.DefaultCommand;

public class CommandAcceptor implements Acceptor{
	static int max=1;
//	@TMProperty
//	private String say;
	public List<Command> receive() {
		List<Command> commands = new ArrayList<Command>();
		if(max==1){
			max++;
			DefaultCommand dc = new DefaultCommand();
//    		dc.setProcessPlanPath("C:\\Users\\z00211682\\Desktop\\FileReader_JDBCWriter.xml");
			String pluginFile = CommandAcceptor.class.getClassLoader().getResource("test.xml").getFile();
			dc.init("dealPlan proPlanPath="+pluginFile);
			commands.add(dc);
			return commands;
		}
		else{
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	public Acceptor init() {
		// TODO Auto-generated method stub
		return this;
	}
	
}
