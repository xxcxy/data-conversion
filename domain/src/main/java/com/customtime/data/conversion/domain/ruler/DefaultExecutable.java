package com.customtime.data.conversion.domain.ruler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.reader.TerminatReader;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;
import com.customtime.data.conversion.plugin.writer.TerminatWriter;

public class DefaultExecutable implements TerminatExecutable{

	private TerminatRunRuler terminatRunRuler;
	private RecodesKeeper recodesKeeper;
	private TerminatRuleRunnable terminatRuleRunnable;
	private ClassLoader classLoader;
	
	private static final Log logger = LogFactory.getLog(DefaultExecutable.class);
	
	public TerminatExecutable execute() {
		terminatRuleRunnable.init();
		try {
			if(terminatRuleRunnable instanceof TerminatWriter){
				Method method = terminatRuleRunnable.getClass().getMethod("writing", RecodesKeeper.class);
				method.invoke(terminatRuleRunnable, recodesKeeper);
			}else if(terminatRuleRunnable instanceof TerminatReader){
				Method method = terminatRuleRunnable.getClass().getMethod("reading", RecodesKeeper.class);
				method.invoke(terminatRuleRunnable, recodesKeeper);
			}
		}catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		terminatRuleRunnable.destroy();
		logger.info("process deal done!");
		return this;
	}

	public TerminatExecutable setRuler(TerminatRunRuler trr) {
		terminatRunRuler = trr;
		return this;
	}

	public TerminatExecutable setRuleRunnable(TerminatRuleRunnable terminatRuleRunnable) {
		this.terminatRuleRunnable = terminatRuleRunnable;
		return this;
	}

	public TerminatExecutable setRecodesKeeper(RecodesKeeper rk) {
		this.recodesKeeper = rk;
		return this;
	}

	public TerminatExecutable setClassLoader(ClassLoader cl) {
		classLoader = cl;
		return this;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void run() {
		while(!terminatRunRuler.isReady()){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		if(terminatRunRuler.isReady()){
			do{
				execute();
			}while(!terminatRunRuler.isEnd());
		}
	}

	public TerminatExecutable destory() {
		recodesKeeper.keepClose();
		return this;
	}

	public Object clone(){
		DefaultExecutable de = null;
		try {
			de = (DefaultExecutable) super.clone();
			de.terminatRuleRunnable = (TerminatRuleRunnable)terminatRuleRunnable.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return de;
	}

	public boolean equals(Object obj){
		if(obj instanceof DefaultExecutable){
			DefaultExecutable dee = (DefaultExecutable)obj;
			if(dee.classLoader.equals(this.classLoader)&&dee.terminatRuleRunnable.equals(this.terminatRuleRunnable)&&dee.recodesKeeper.equals(this.recodesKeeper)&&dee.terminatRunRuler.equals(this.terminatRunRuler))
				return true;
		}
		return false;
	}
	
}
