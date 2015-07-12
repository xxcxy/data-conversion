package com.customtime.data.conversion.domain.ruler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.domain.util.CustomClassLoaderThreadFactory;
import com.customtime.data.conversion.plugin.util.MonitorUtil;

public class DefaultRuleRunner implements TerminatRuleRunner {

	private List<TerminatExecutable> terminatInExecutables;
	private Map<ThreadPoolExecutor,List<TerminatExecutable>> terminatOutExecutables;
	private Map<Object,List<Method>> finishCallbakMonitors;
	private Map<Object,List<Method>> postCallbakMonitors;
	private Map<Object,List<Method>> preCallbakMonitors;
	private ThreadPoolExecutor threadPoolInExecutor;
	private List<ThreadPoolExecutor> threadPoolOutExecutor;
	private final static Log logger = LogFactory.getLog(DefaultRuleRunner.class);
	public DefaultRuleRunner(){
		terminatInExecutables = new ArrayList<TerminatExecutable>();
		terminatOutExecutables = new HashMap<ThreadPoolExecutor,List<TerminatExecutable>>();
		threadPoolInExecutor =  new ThreadPoolExecutor(2,5,3,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(30000),new ThreadPoolExecutor.CallerRunsPolicy());
		threadPoolOutExecutor = new ArrayList<ThreadPoolExecutor>();

	}
	
	public TerminatRuleRunner addInExecutable(TerminatExecutable trr) {
		terminatInExecutables.add(trr);
		threadPoolInExecutor.setThreadFactory(new CustomClassLoaderThreadFactory(trr.getClassLoader()));
		return this;
	}

	public TerminatRuleRunner addInExecutable(List<TerminatExecutable> trrs) {
		for(TerminatExecutable e:trrs)
			terminatInExecutables.add(e);
		threadPoolInExecutor.setThreadFactory(new CustomClassLoaderThreadFactory(trrs.get(0).getClassLoader()));
		return this;
	}

	public TerminatRuleRunner addOutExecutable(TerminatExecutable trr) {
		ThreadPoolExecutor threadPoolOutExecutort = new ThreadPoolExecutor(3,5,3,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(30000),new ThreadPoolExecutor.CallerRunsPolicy());
		threadPoolOutExecutort.setThreadFactory(new CustomClassLoaderThreadFactory(trr.getClassLoader()));
		terminatOutExecutables.put(threadPoolOutExecutort,Arrays.asList(trr));
		threadPoolOutExecutor.add(threadPoolOutExecutort);
		return this;
	}

	public TerminatRuleRunner addOutExecutable(List<TerminatExecutable> trrs) {
		ThreadPoolExecutor threadPoolOutExecutort = new ThreadPoolExecutor(3,5,3,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(30000),new ThreadPoolExecutor.CallerRunsPolicy());
		threadPoolOutExecutort.setThreadFactory(new CustomClassLoaderThreadFactory(trrs.get(0).getClassLoader()));
//		terminatOutExecutables.put(threadPoolOutExecutort,Arrays.asList(trrs.get(0),(TerminatExecutable)trrs.get(0).clone(),(TerminatExecutable)trrs.get(0).clone()));
		terminatOutExecutables.put(threadPoolOutExecutort,trrs);
		threadPoolOutExecutor.add(threadPoolOutExecutort);
		return this;
	}

	public TerminatRuleRunner setFinishCallbakMonitors(Map<Object,List<Method>> finishCallbakMonitors){
		this.finishCallbakMonitors = finishCallbakMonitors;
		return this;
	}
	public TerminatRuleRunner setPostCallbakMonitors(Map<Object,List<Method>> postCallbakMonitors){
		this.postCallbakMonitors = postCallbakMonitors;
		return this;
	}
	public TerminatRuleRunner setPreCallbakMonitors(Map<Object,List<Method>> preCallbakMonitors){
		this.preCallbakMonitors = preCallbakMonitors;
		return this;
	}
	
	public TerminatRuleRunner execute() {
		// the Pre method do
		doMethod(preCallbakMonitors,false,null);
		
		for(TerminatExecutable tale:terminatInExecutables){
			threadPoolInExecutor.execute(tale);
		}
		for(Entry<ThreadPoolExecutor,List<TerminatExecutable>> tale:terminatOutExecutables.entrySet()){
			for(TerminatExecutable tet:tale.getValue())
				tale.getKey().execute(tet);
		}
		threadPoolInExecutor.shutdown();
		for(ThreadPoolExecutor te:threadPoolOutExecutor)
			te.shutdown();
		MonitorUtil moUtil = new MonitorUtil().setPluginMonitors();
		//TODO new Thread to do TIMEEXEC method
		while(true){
			if(threadPoolInExecutor.isTerminated()){
				logger.debug("read end!");
				ResourceContext.getPluginConfig().getRecodesKeeper().keepClose();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(writerTerminated()){
				logger.debug("writer end!");
				if(!threadPoolInExecutor.isTerminated())
					logger.error("writer end before reader!");
				break;
			}
				
		}
		// the FINISHEXEC method do
		doMethod(finishCallbakMonitors,true,moUtil);
		// the Post method do
		doMethod(postCallbakMonitors,false,null);
		return this;
	}

	private boolean writerTerminated(){
		for(ThreadPoolExecutor tpe:threadPoolOutExecutor){
			if(!tpe.isTerminated())
				return false;
		}
		return true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doMethod(Map<Object,List<Method>> callBackMethod,boolean isMonistor,MonitorUtil moUtil){
		for(Entry<Object,List<Method>> ent:callBackMethod.entrySet()){
			Object obj = ent.getKey();
			for(Method method:ent.getValue()){
				Class[] clazzes = method.getParameterTypes();
				Object[] objs = new Object[clazzes.length];
				for(int i=0,size=clazzes.length;i<size;i++){
					if(isMonistor && clazzes[i].isAssignableFrom(MonitorUtil.class))
						objs[i] = moUtil;
					else
						objs[i] = null;
				}
				try {
					ClassLoader c1 = Thread.currentThread().getContextClassLoader();
					ClassLoader c2 = getClassLoader(obj);
					if(c2!=null)
						Thread.currentThread().setContextClassLoader(c2);
					method.invoke(obj,objs);
					Thread.currentThread().setContextClassLoader(c1);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private ClassLoader getClassLoader(Object obj){
		PluginConfig pc = ResourceContext.getPluginConfig();
		return pc.getClassLoader(obj);
	}
}
