package com.customtime.data.conversion.domain.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.acceptor.Acceptor;
import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.config.EngineConfig;
import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.domain.controller.Controller;
import com.customtime.data.conversion.domain.plan.ProcessPlan;
import com.customtime.data.conversion.domain.util.QuartzManager;
import com.customtime.data.conversion.domain.util.ShutDownWork;
import com.customtime.data.conversion.domain.util.TimerJob;
import com.customtime.data.conversion.plugin.exception.PluginConfigException;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;

public class TerminatorEngine {
	private static final Log logger = LogFactory.getLog(TerminatorEngine.class);
	private static final String TempFilePaht = "pid.temp";
	private static final String AcceptName = "acceptName:";
	private Acceptor acceptor;
	private EngineConfig eConfig;
	private ThreadPoolExecutor threadPoolExecutor;
	
	public TerminatorEngine() {
		// eConfig = new EngineConfig();
		eConfig = ResourceContext.getEngineConfig();
		threadPoolExecutor = new ThreadPoolExecutor(8, 10, 3, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(30000));
		try {
			acceptor = eConfig.getSingleAcceptor().init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO
	}
	
	public void start() {
		for (Command comm : eConfig.getTaskCommand()) {
			co2pp(comm);
		}
		while (true) {
			List<Command> commands = acceptor.receive();
			if (commands == null)
				continue;
			for (Command command : commands) {
				ProcessPlan processPlan = co2pp(command);
				if (processPlan != null) {
					// Thread t = new
					// Thread(getController().setPlan(processPlan));
					// t.setPriority(2);
					// t.start();
					threadPoolExecutor.execute(getController().setPlan(
							processPlan));
				}
			}
		}
	}
	
	private Controller getController() {
		return eConfig.getController().setRuleRunner(eConfig.getRuleRunner());
	}
	
	private ProcessPlan co2pp(Command command) {
		String timerS = command.getTimeSchedule();
		if (timerS != null) {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put(TimerJob.COMMAND_KEY, command);
			String jobName = command.getCommandProps().get(TimerJob.JOB_NAME);
			if (jobName == null || "".equals(jobName))
				jobName = command.getProcessPlanPath();
			QuartzManager.addJob(jobName, TimerJob.class, timerS, param);
			return null;
		}
		ProcessPlan processPlan = eConfig.getProcessPlan();
		PluginMonitor pm = eConfig.getPluginMonitor();
		PluginConfig pc = eConfig.getPluginConfig();
		try {
			pc.setCommandProps(command.getCommandProps());
			pc.setProcessPlanPath(command.getProcessPlanPath());
			// TODO
		} catch (PluginConfigException e) {
			e.printStackTrace();
			return null;
		}
		processPlan.setPluginConfig(pc);
		processPlan.setPluginMonitor(pm);
		return processPlan;
	}
	
	public static void main(String[] args) {
		startTE();
	}
	
	public static void startTE() {
		File pidFile = new File(TempFilePaht);
		if (pidFile.exists()) {
			logger.error("TE has run , can not run again!!!");
		} else {
			Runtime.getRuntime()
					.addShutdownHook(new ShutDownWork(TempFilePaht));
			try {
				TerminatorEngine te = new TerminatorEngine();
				String acceptName = te.eConfig.getSingleAcceptor().getClass()
						.getName();
				createPid(pidFile, acceptName);
				te.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void createPid(File pidFile, String acceptName)
			throws IOException {
		if (pidFile.createNewFile()) {
			PrintWriter pw = null;
			try {
				String procName = ManagementFactory.getRuntimeMXBean()
						.getName();
				pw = new PrintWriter(pidFile);
				pw.println(procName);
				pw.println(AcceptName + acceptName);
			} finally {
				pw.close();
			}
		}
	}
}
