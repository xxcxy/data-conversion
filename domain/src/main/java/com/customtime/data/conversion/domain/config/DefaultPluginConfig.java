package com.customtime.data.conversion.domain.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.customtime.data.conversion.domain.annotation.InjectMonitor;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.domain.handler.TerminatHandler;
import com.customtime.data.conversion.domain.ruler.TerminatExecutable;
import com.customtime.data.conversion.domain.ruler.TerminatRunRuler;
import com.customtime.data.conversion.domain.util.TMClasspathLoader;
import com.customtime.data.conversion.plugin.addition.TerminatRunable;
import com.customtime.data.conversion.plugin.annotaion.AOPExecuteType;
import com.customtime.data.conversion.plugin.annotaion.AOPMethod;
import com.customtime.data.conversion.plugin.annotaion.CallMonitor;
import com.customtime.data.conversion.plugin.annotaion.CallbackType;
import com.customtime.data.conversion.plugin.exception.PluginConfigException;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.ruler.TerminatRuleRunnable;

public class DefaultPluginConfig implements PluginConfig {
	
	@SuppressWarnings("unused")
	private final static Log logger=LogFactory.getLog(DefaultPluginConfig.class);
	
	private Map<TerminatExecutable, List<TerminatHandler>> readHandlers;
	private Map<TerminatExecutable, List<TerminatHandler>> writeHandlers;
	private Map<TerminatExecutable,List<TerminatExecutable>> readers;
	private Map<TerminatExecutable,List<TerminatExecutable>> writers;
	private Map<TerminatRuleRunnable,TerminatExecutable> trr2ter;
	private Map<TerminatRuleRunnable,PluginMonitor> monitors;
	private Map<Object,Map<String,String>> properties;
	private Map<String,Object> pluginId2Object;
	private Map<Object,List<Method>> monitorCallBacks;
	private Map<Object,List<Method>> postMethod;
	private Map<Object,List<Method>> preMethod;
	private Map<String,Map<String,String>> commandProps;
	private RecodesKeeper recodesKeeper;
	private Map<String,TerminatRunable> pluginThreaders;

	public RecodesKeeper getRecodesKeeper() {
		return recodesKeeper;
	}

	public void setRecodesKeeper(RecodesKeeper recodesKeeper) {
		this.recodesKeeper = recodesKeeper;
	}
	
	public void setCommandProps(Map<String,String> commandPropsParam){
		commandProps = new HashMap<String,Map<String,String>>();
		for(Entry<String,String> param:commandPropsParam.entrySet()){
			String key = param.getKey();
			int index = key.indexOf('.');
			if(index>0){
				String obj = key.substring(0,index);
				String prop = key.substring(index+1);
				if(commandProps.containsKey(obj)){
					commandProps.get(obj).put(prop, param.getValue());
				}else{
					Map<String,String> objMap = new HashMap<String,String>();
					objMap.put(prop, param.getValue());
					commandProps.put(obj, objMap);
				}
			}
		}
		ResourceContext.setPlanParam(commandPropsParam);
	}
	
	@SuppressWarnings({ "unchecked" })
	public void setProcessPlanPath(String path) throws PluginConfigException{
		SAXReader reader = new SAXReader();
		File file = new File(path);
		if(!file.exists())
			throw new PluginConfigException("pluginConfig file not found!");
		recodesKeeper = ResourceContext.getEngineConfig().getRecodesKeeper().init(null);
		Document xmlDoc = null;
		try {
			xmlDoc = reader.read(file);
			Element root = xmlDoc.getRootElement();
			List<Element> els = root.elements("plugin");
			Map<TerminatExecutable, List<TerminatHandler>> rhrs = new HashMap<TerminatExecutable, List<TerminatHandler>>();
			Map<TerminatExecutable, List<TerminatHandler>> whrs = new HashMap<TerminatExecutable, List<TerminatHandler>>();
			Map<TerminatRuleRunnable,TerminatExecutable> trr2te = new HashMap<TerminatRuleRunnable,TerminatExecutable>();
			writers = new HashMap<TerminatExecutable, List<TerminatExecutable>>(); 
			readers = new HashMap<TerminatExecutable, List<TerminatExecutable>>(); 
			monitors = new HashMap<TerminatRuleRunnable,PluginMonitor>();
			properties = new HashMap<Object,Map<String,String>>();
			pluginId2Object = new HashMap<String,Object>();
			monitorCallBacks = new HashMap<Object,List<Method>>();
			postMethod = new HashMap<Object,List<Method>>();
			preMethod = new HashMap<Object,List<Method>>();
			pluginThreaders = new HashMap<String,TerminatRunable>();
			Map<TerminatExecutable,Integer> writerT= new HashMap<TerminatExecutable,Integer>();
			Map<TerminatExecutable,Integer> readerT= new HashMap<TerminatExecutable,Integer>();
			for(Element elemt:els){
				String jarPath = elemt.elementTextTrim("jar-dir");
				Element mainCl = elemt.element("main-class");
				if(mainCl==null)
					throw new PluginConfigException("every plugin must have the main-class!");
				String mainClass = mainCl.attributeValue("class");//elemt.elementTextTrim("main-class");
				String plugId = mainCl.attributeValue("ID");
				if(mainClass==null || "".equals(mainClass)||plugId==null || "".equals(plugId))
					throw new PluginConfigException("every plugin must have the mainclass and id!");
				String type = elemt.attributeValue("type");
				if("reader".equals(type)||"writer".equals(type)){
					TerminatExecutable te = ResourceContext.getEngineConfig().getTerminatExecutable();
					ClassLoader cl = TMClasspathLoader.getNewClassLoader();
					if(jarPath!=null&&!"".equals(jarPath))
						TMClasspathLoader.loadClasspath(jarPath,cl);
					TerminatRuleRunnable tr = ResourceContext.getObject(TerminatRuleRunnable.class, mainClass,cl);
					te.setRuleRunnable(tr);
					trr2te.put(tr,te);
					te.setRecodesKeeper(recodesKeeper);
					te.setClassLoader(cl);
					te.setRuler(getRuler(elemt,root,cl));
					if("reader".equals(type)){
						rhrs.put(te, getHandlers(elemt,root,cl));
						readerT.put(te, getNum(elemt));
					}else if("writer".equals(type)){
						whrs.put(te, getHandlers(elemt,root,cl));
						recodesKeeper.addWriteRunnable(tr);
						moreWriter(te,elemt);
						writerT.put(te, getNum(elemt));
					}
					Map<String,String> props = getProperties(mainCl);
					props.putAll(getPropertyFromCommand(plugId));
					//TODO
					properties.put(tr,props);
					pluginId2Object.put(plugId, tr);
				}else if("threader".equals(type)){
					ClassLoader cl = TMClasspathLoader.getNewClassLoader();
					if(jarPath!=null&&!"".equals(jarPath))
						TMClasspathLoader.loadClasspath(jarPath,cl);
					TerminatRunable tr = ResourceContext.getObject(TerminatRunable.class, mainClass,cl);
					pluginThreaders.put(plugId, tr);
					Map<String,String> props = getProperties(mainCl);
					props.putAll(getPropertyFromCommand(plugId));
					//TODO
					properties.put(tr,props);
				}
			}
			readHandlers = rhrs;
			writeHandlers = whrs;
			trr2ter = trr2te;
			setProperties();
			injectMonitor();
			injectMonitor2Handler();
			registerMethods();
			cloneAgain(writerT,readerT);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new PluginConfigException(e.getMessage());
		} catch (DocumentException e) {
			e.printStackTrace();
			throw new PluginConfigException(e.getMessage());
		} catch (ClassNotFoundException e){
			e.printStackTrace();
			throw new PluginConfigException(e.getMessage());
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new PluginConfigException(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new PluginConfigException(e.getMessage());
		}
		
	}
	@SuppressWarnings("unchecked")
	private List<TerminatHandler> getHandlers(Element elemt,Element root,ClassLoader cl) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		List<TerminatHandler> thr = new ArrayList<TerminatHandler>();
		for(Element ele:(List<Element>)elemt.elements("handler-id")){
			Element ehander = root.elementByID(ele.getTextTrim());
			String jarPath = ehander.elementTextTrim("jar-dir");
			if(jarPath!=null&&!"".equals(jarPath))
				TMClasspathLoader.loadClasspath(jarPath,cl);
			Element elem = ehander.element("main-class");
			String plugId = elem.attributeValue("ID");
			TerminatHandler thlr = ResourceContext.getObject(TerminatHandler.class,elem.attributeValue("class"),cl);
			Map<String,String> props = getProperties(elem);
			props.putAll(getPropertyFromCommand(plugId));
			//TODO
			properties.put(thlr,props);
			pluginId2Object.put(plugId, thlr);
			thr.add(thlr);
		}
		return thr;
	}
	private TerminatRunRuler getRuler(Element elemt,Element root,ClassLoader cl)throws PluginConfigException, ClassNotFoundException, InstantiationException, IllegalAccessException{
		String rId = elemt.elementTextTrim("rule-id");
		Element ele = root.elementByID(rId);
		if(ele==null)
			throw new PluginConfigException("can not Contain runruler");
		String jarPath = ele.elementTextTrim("jar-dir");
		if(jarPath!=null&&!"".equals(jarPath))
			TMClasspathLoader.loadClasspath(jarPath,cl);
		Element elem = ele.element("main-class");
		TerminatRunRuler rtrr = ResourceContext.getObject(TerminatRunRuler.class, elem.attributeValue("class"),cl);
		Map<String,String> props = getProperties(elem);
		String plugId = elem.attributeValue("ID");
		props.putAll(getPropertyFromCommand(plugId));
		//TODO
		properties.put(rtrr,props);
		return rtrr;
	}
	private Map<String,String> getPropertyFromCommand(String pluginId){
		Map<String,String> rMap = new HashMap<String,String>();
		if(commandProps!=null&&commandProps.get(pluginId)!=null)
			rMap.putAll(commandProps.get(pluginId));
		return rMap;
	}
	private void injectMonitor(){
		for(TerminatRuleRunnable tr:trr2ter.keySet()){
			Field field = getMonitorField(tr);
			try{
				if(field!=null){
					PluginMonitor pm = ResourceContext.getEngineConfig().getPluginMonitor();
					field.setAccessible(true);
					field.set(tr,pm);
					monitors.put(tr, pm);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
	}
	private Field getMonitorField(Object obj){
		Field[] fields = obj.getClass().getDeclaredFields();
		for(Field field:fields){
			if(field.isAnnotationPresent(InjectMonitor.class)){
				 if(field.getType().isAssignableFrom(PluginMonitor.class)){
					 return field;
				 }
			}
		}
		return null;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Method> getAnnotationMethod(Class clazz,Class annotationType){
		List<Method> rML = new ArrayList<Method>();
		Method[] methods = clazz.getDeclaredMethods();
		for(Method method:methods){
			if(method.isAnnotationPresent(annotationType))
				rML.add(method);
		}
		return rML;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Method> getAnnotationMethod(Class clazz,Class annotationType,AOPExecuteType type){
		List<Method> rML = new ArrayList<Method>();
		List<Method> rMLt = getAnnotationMethod(clazz,AOPMethod.class);
		for(Method method:rMLt){
			AOPMethod cm = method.getAnnotation(annotationType);
			if(cm!=null && cm.value().equals(type)){
				rML.add(method);
			}
		}
		return rML;
	}
	@SuppressWarnings("unchecked")
	private void injectMonitor2Handler(){
		for(Entry<TerminatRuleRunnable,TerminatExecutable> etrr:trr2ter.entrySet()){
			PluginMonitor pm = monitors.get(etrr.getKey());
			for(TerminatHandler th:getCombinationList(readHandlers.get(etrr.getValue()),writeHandlers.get(etrr.getValue()))){
				Field field = getMonitorField(th);
				try {
					if(field!=null){
						if(pm==null){
							pm = ResourceContext.getEngineConfig().getPluginMonitor();
							monitors.put(etrr.getKey(),pm);
						}
						field.setAccessible(true);
						field.set(th,pm);
					}
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void registerMethods(){
		for(Entry<String,Object> entry:pluginId2Object.entrySet()){
			Object obj = entry.getValue();
			if(!monitorCallBacks.containsKey(obj)){
				List<Method> methods = getAnnotationMethod(obj.getClass(),CallMonitor.class);
				monitorCallBacks.put(obj, methods);
			}
			if(!postMethod.containsKey(obj)){
				List<Method> methods = getAnnotationMethod(obj.getClass(),AOPMethod.class,AOPExecuteType.POST);
				postMethod.put(obj, methods);
			}
			if(!preMethod.containsKey(obj)){
				List<Method> methods = getAnnotationMethod(obj.getClass(),AOPMethod.class,AOPExecuteType.PRE);
				preMethod.put(obj, methods);
			}
		}
	}
	private <T> List<T> getCombinationList(List<T>...lists){
		List<T> arr = new ArrayList<T>();
		for(List<T> list:lists){
			if(list!=null)
				arr.addAll(list);
		}
		return arr;
	}
	@SuppressWarnings("unchecked")
	private Map<String,String> getProperties(Element ele){
		Map<String,String> properties = new HashMap<String,String>();
		for(Element elem:(List<Element>)ele.elements("property")){
			properties.put(elem.attributeValue("name"),elem.getTextTrim());
		}
		return properties;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setProperties(){
		try {
			for(Entry<Object,Map<String,String>> obje:properties.entrySet()){
				Object obj = obje.getKey();
				Map<String,String> props = obje.getValue();
				ResourceContext.setProperties((Class)obj.getClass(), props, obj);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
	}
	
	private Integer getNum(Element elemt){
		String num = elemt.elementTextTrim("thread-num");
		if(num!=null && !"".equals(num)){
			int len = Integer.parseInt(num);
			return len;
		}
		return 1;
	}
	
	private void moreWriter(TerminatExecutable te,Element elemt){
		List<TerminatExecutable> tes = new ArrayList<TerminatExecutable>();
		tes.add(te);
		String num = elemt.elementTextTrim("thread-num");
		if(num!=null && !"".equals(num)){
			int len = Integer.parseInt(num);
			for(int i=1;i<len;i++)
				tes.add((TerminatExecutable)te.clone());
		}
		writers.put(te, tes);
	}
	
	private void cloneAgain(Map<TerminatExecutable,Integer> writerT,Map<TerminatExecutable,Integer> readerT){
		for(Entry<TerminatExecutable,Integer> alls:writerT.entrySet()){
			List<TerminatExecutable> tes = new ArrayList<TerminatExecutable>();
			TerminatExecutable key = alls.getKey();
			tes.add(key);
			for(int i=1,len=alls.getValue();i<len;i++)
				tes.add((TerminatExecutable)key.clone());
			writers.put(key,tes);
		}
		for(Entry<TerminatExecutable,Integer> alls:readerT.entrySet()){
			List<TerminatExecutable> tes = new ArrayList<TerminatExecutable>();
			TerminatExecutable key = alls.getKey();
			tes.add(key);
			for(int i=1,len=alls.getValue();i<len;i++)
				tes.add((TerminatExecutable)key.clone());
			readers.put(key,tes);
		}
	}
	
	public List<TerminatExecutable> getReaders() {
		//return new ArrayList<TerminatExecutable>(readHandlers.keySet());
		for(List<TerminatExecutable> rt:readers.values())
			return rt;
		return null;
	}

	public List<List<TerminatExecutable>> getWriters() {
		return new ArrayList<List<TerminatExecutable>>(writers.values());
		
	}

	public Map<TerminatExecutable, List<TerminatHandler>> getReadHandler() {
		return readHandlers;
	}

	public Map<TerminatExecutable, List<TerminatHandler>> getWriteHandler() {
		return writeHandlers;
	}

	public List<TerminatHandler> getReadHandler(TerminatExecutable tr) {
		return readHandlers.get(tr);
	}

	public List<TerminatHandler> getWriteHandler(TerminatExecutable tw) {
		return writeHandlers.get(tw);
	}
	
	public List<TerminatHandler> getReadHandler(TerminatRuleRunnable tr) {
		return readHandlers.get(trr2ter.get(tr));
	}

	public List<TerminatHandler> getWriteHandler(TerminatRuleRunnable tw) {
		return writeHandlers.get(trr2ter.get(tw));
	}

	public List<PluginMonitor> getPluginMonitor(){
		return new ArrayList<PluginMonitor>(monitors.values());
	}
	
	public Map<Object, List<Method>> getFinishCallBakcMonit() {
		Map<Object,List<Method>> rMap = new HashMap<Object,List<Method>>();
		for(Entry<Object,List<Method>> entry:monitorCallBacks.entrySet()){
			Object obj = entry.getKey();
			for(Method method:entry.getValue()){
				CallMonitor cm = method.getAnnotation(CallMonitor.class);
				if(cm!=null && cm.value().equals(CallbackType.FINISHEXEC)){
					List<Method> lm = rMap.get(obj);
					if(lm==null){
						lm = new ArrayList<Method>();
						rMap.put(obj, lm);
					}
					lm.add(method);
				}
			}
		}
		return rMap;
	}
	
	public Map<Object, List<Method>> getPostMethod(){
		return postMethod;
	}
	
	public Map<Object, List<Method>> getPreMethod(){
		return preMethod;
	}
	
	public TerminatRunable getPluginThread(String key) {
		return pluginThreaders.get(key);
	}
	
	public List<TerminatRunable> getAllPluginThread(){
		return new ArrayList<TerminatRunable>(pluginThreaders.values());
	}

	public ClassLoader getClassLoader(Object obj){
		for(Entry<TerminatRuleRunnable,TerminatExecutable> tre:trr2ter.entrySet()){
			if(tre.getKey().equals(obj)){
				return tre.getValue().getClassLoader();
			}
		}
		for(Entry<TerminatExecutable, List<TerminatHandler>> teh:readHandlers.entrySet()){
			for(TerminatHandler th:teh.getValue()){
				if(th.equals(obj))
					return teh.getKey().getClassLoader();
			}
		}
		for(Entry<TerminatExecutable, List<TerminatHandler>> teh:writeHandlers.entrySet()){
			for(TerminatHandler th:teh.getValue()){
				if(th.equals(obj))
					return teh.getKey().getClassLoader();
			}
		}
		return null;
	}
}
