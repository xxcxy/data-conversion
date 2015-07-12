package com.customtime.data.conversion.domain.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.customtime.data.conversion.domain.acceptor.Acceptor;
import com.customtime.data.conversion.domain.acceptor.command.Command;
import com.customtime.data.conversion.domain.config.EngineConfig;
import com.customtime.data.conversion.domain.config.PluginConfig;
import com.customtime.data.conversion.domain.controller.Controller;
import com.customtime.data.conversion.domain.plan.ProcessPlan;
import com.customtime.data.conversion.domain.ruler.TerminatExecutable;
import com.customtime.data.conversion.domain.ruler.TerminatRuleRunner;
import com.customtime.data.conversion.domain.temporary.Temporary;
import com.customtime.data.conversion.domain.util.TMClasspathLoader;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.context.PluginContext;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.pool.AbstractPool;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;


public class ResourceContext {
	private static final Log logger = LogFactory.getLog(ResourceContext.class);
	private static EngineConfig eConfig;
	private static AbstractPool aPool;
	private final static InheritableThreadLocal<PluginConfig> pluginConfig = new InheritableThreadLocal<PluginConfig>();
	private final static InheritableThreadLocal<Map<String,Object>> planAttr = new InheritableThreadLocal<Map<String,Object>>();
	private final static InheritableThreadLocal<Map<String,String>> planParam = new InheritableThreadLocal<Map<String,String>>();
	
	static{
		eConfig = parseEngineConfig(ResourceContext.class.getClassLoader().getResourceAsStream("engine-config.xml"));
		PluginContext.setInstance(new PluginContextImpl());
	}
	static void setEngineConfig(EngineConfig engineConfig){
		eConfig = engineConfig;
	}
	public static EngineConfig getEngineConfig(){
		return eConfig;
	}
	
	static EngineConfig parseEngineConfig(String path){
		return parseEngineConfig(new File(path));
	}
	static EngineConfig parseEngineConfig(File file){
		try {
			return parseEngineConfig(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	static EngineConfig parseEngineConfig(InputStream file){
		SAXReader reader = new SAXReader();
		if(file==null){
			file = ResourceContext.class.getClassLoader().getResourceAsStream("engine-default-config.xml");
			logger.info("can not find engine-config.xml,use the engine-default-config.xml");
		}
		Document xmlDoc = null;
		try {
			xmlDoc = reader.read(file);
			Element root = xmlDoc.getRootElement();
			Element els = root.element("core-implement");
			EngineConfig engconf = new EngineConfig();
			Map<Class<?>,Map<String,String>> properties = new HashMap<Class<?>,Map<String,String>>();
			Map<String,String> tasks = new HashMap<String,String>();
			engconf.setAcceptor(getObject(Acceptor.class,els.element("acceptor"),properties));
			engconf.setController(getObject(Controller.class,els.element("controller"),properties));
			engconf.setRuleRunner(getObject(TerminatRuleRunner.class,els.element("rule-runner"),properties));
			engconf.setRecodesKeeper(getObject(RecodesKeeper.class,els.element("recodes-keeper"),properties));
			engconf.setCommand(getObject(Command.class,els.element("command"),properties));
			engconf.setProcessPlan(getObject(ProcessPlan.class,els.element("process-plan"),properties));
			engconf.setRecode(getObject(Recode.class,els.element("recode"),properties));
			engconf.setPluginConfig(getObject(PluginConfig.class,els.element("plugin-config"),properties));
			engconf.setPluginMonitor(getObject(PluginMonitor.class,els.element("plugin-monitor"),properties));
			engconf.setTerminatExecutable(getObject(TerminatExecutable.class,els.element("rule-execute"),properties));
			engconf.setTemporary(getObject(Temporary.class,els.element("temporary"),properties));
			engconf.setProperties(properties);
			aPool = getObject(AbstractPool.class,els.element("CachePool"),properties);
			Element schedulers = root.element("scheduler");
			if(schedulers!=null){
				@SuppressWarnings("unchecked")
				List<Element> task = schedulers.elements("task");
				if(task!=null){
					for(Element e:task){
						tasks.put(e.getTextTrim(), e.getTextTrim());
					}
				}
			}
			engconf.setTasks(tasks);
			return engconf;
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getObject(Class<T> clazz,Element ele,Map<Class<?>,Map<String,String>> properties) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		T t = getObject(clazz,ele.attributeValue("class"));
		Map<String,String> propertiesV = new HashMap<String,String>();
		for(Element elem:(List<Element>)ele.elements("property")){
			propertiesV.put(elem.attributeValue("name"),elem.getTextTrim());
		}
		properties.put(t.getClass(), propertiesV);
		return setProperties((Class<T>)t.getClass(),propertiesV,t);
	}
	public static PluginConfig getPluginConfig(){
		return pluginConfig.get();
	}
	
	public static void setPluginConfig(PluginConfig pc){
		pluginConfig.set(pc);
	}
	public static boolean removeThreadObject(){
		pluginConfig.remove();
		planAttr.remove();
		planParam.remove();
		return true;
	}
	
	public static void setPlanAttr(){
		Map<String,Object> attrMap = new HashMap<String,Object>();
		planAttr.set(attrMap);
	}
	
	public static void setPlanParam(Map<String,String> attrMap){
		planParam.set(attrMap);
	}
	
	public static Command getCommand(){
		return eConfig.getCommand();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getObject(Class<T> ct,String name ) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class<?> cla = TMClasspathLoader.loadClass(name);
		if(ct.isAssignableFrom(cla))
			return (T)cla.newInstance();
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T getObject(Class<T> ct,String name,ClassLoader cl) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class<?> cla = TMClasspathLoader.loadClass(name,cl);
		if(ct.isAssignableFrom(cla))
			return (T)cla.newInstance();
		else
			return null;
	}
	
	public static <T> T setProperties(Class<T> clazz,Map<String,String> props,T t) throws IllegalArgumentException, IllegalAccessException{
		Field[] fields = clazz.getDeclaredFields();
		for(Field field:fields){
			if(field.isAnnotationPresent(TMProperty.class)){
				TMProperty tmp = field.getAnnotation(TMProperty.class);
				String pname = tmp.name();
				if(null == pname || "".equals(pname))
					pname = field.getName();
				String pvalue = props.get(pname);
				if(null == pvalue||"".equals(pvalue))
					pvalue = tmp.defaultValue();
				field.setAccessible(true);
				Class<?> typeClass = field.getType(); 
				Object value = ResourceContext.convert(pvalue,typeClass);
				field.set(t,value);
			}
		}
		return t;
	}
	public static Object convert(String value,Class<?> type){
		if("".equals(value)&&!type.equals(String.class)){
			value = "0";
		}
		if (type.equals(byte.class) || type.equals(Byte.class)) {
            return value.getBytes()[0];
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.parseShort(value);
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.parseLong(value);
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.parseFloat(value);
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        }
        if(type.equals(boolean.class)|| type.equals(Boolean.class)){
        	if("true".equalsIgnoreCase(value))
        		return true;
        	return false;
        }
        if(type.equals(String.class)){
        	return value;
        }
        return value;
	}
	public static Object getAttr(String key){
		Map<String,Object> attrMap = planAttr.get();
		if(attrMap!=null)
			return attrMap.get(key);
		return null;
	}
	public static boolean setAttr(String key,Object obj){
		Map<String,Object> attrMap = planAttr.get();
		if(attrMap!=null){
			attrMap.put(key, obj);
			return true;
		}
		return false;
	}
	public static String getParam(String key){
		Map<String,String> paramMap = planParam.get();
		if(paramMap!=null)
			return paramMap.get(key);
		return null; 
	}
	public static AbstractPool getPool(){
		return aPool;
	}
}
