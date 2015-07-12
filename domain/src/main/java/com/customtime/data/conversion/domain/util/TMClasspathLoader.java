package com.customtime.data.conversion.domain.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TMClasspathLoader {
	 /** URLClassLoader的addURL方法 */
    private static Method addURL = initAddMethod();
    private static final Log logger = LogFactory.getLog(TMClasspathLoader.class);

//    private static URLClassLoader classloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//    private static URLClassLoader classloader = new PluginAppClassLoader();
    /**
     * 初始化addUrl 方法.
     * @return 可访问addUrl方法的Method对象
     */
    private static Method initAddMethod() {
        try {
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            add.setAccessible(true);
            return add;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载jar classpath。
     */
    public static void loadClasspath() {
        List<String> files = getJarFiles();
        for (String f : files) {
            loadClasspath(f);
        }

        List<String> resFiles = getResFiles();

        for (String r : resFiles) {
            loadResourceDir(r);
        }
    }

    public static void loadClasspath(String filepath) {
        File file = new File(filepath);
        loopFiles(file);
    }

    public static void loadClasspath(String filepath,ClassLoader cl) {
        File file = new File(filepath);
        loopFiles(file,cl);
    }
    
    private static void loadResourceDir(String filepath) {
        File file = new File(filepath);
        loopDirs(file);
    }

    /**    
     * 循环遍历目录，找出所有的资源路径。
     * @param file 当前遍历文件
     */
    private static void loopDirs(File file) {
        // 资源文件只加载路径
        if (file.isDirectory()) {
            addURL(file);
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopDirs(tmp);
            }
        }
    }

    /**    
     * 循环遍历目录，找出所有的jar包。
     * @param file 当前遍历文件
     */
    private static void loopFiles(File file) {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp);
            }
        }
        else {
            if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
                addURL(file);
                logger.debug(file);
            }
        }
    }
    
    private static void loopFiles(File file,ClassLoader cl) {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp,cl);
            }
        }
        else {
            if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
                addURL(file,cl);
                logger.debug(file);
            }
        }
    }

    /**
     * 通过filepath加载文件到classpath。
     * @param filePath 文件路径
     * @return URL
     * @throws Exception 异常
     */
    private static void addURL(File file) {
        try {
            addURL.invoke(getClassLoader(), new Object[] { file.toURI().toURL() });
        }
        catch (Exception e) {
        }
    }
    
    private static void addURL(File file,ClassLoader cl) {
        try {
            addURL.invoke(cl, new Object[] { file.toURI().toURL() });
        }
        catch (Exception e) {
        }
    }
    
    /**
     * 通过类全名获得类对象
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(String name) throws ClassNotFoundException {
    	return getClassLoader().loadClass(name);
    }

    public static Class<?> loadClass(String name,ClassLoader cl) throws ClassNotFoundException {
    	return cl.loadClass(name);
    }
    
    /**
     * 从配置文件中得到配置的需要加载到classpath里的路径集合。
     * @return
     */
    private static List<String> getJarFiles() {
        // TODO 从properties文件中读取配置信息略
        return null;
    }

    /**
     * 从配置文件中得到配置的需要加载classpath里的资源路径集合
     * @return
     */
    private static List<String> getResFiles() {
        //TODO 从properties文件中读取配置信息略
        return null;
    }

    public static ClassLoader getClassLoader(){
    	return Thread.currentThread().getContextClassLoader();
    }
    
    public static ClassLoader getNewClassLoader(){
    	return new PluginAppClassLoader();
 //   	return ClassLoader.getSystemClassLoader();
    }
    
    public static void setClassLoader(){
    	Thread.currentThread().setContextClassLoader(getNewClassLoader());
    }
    
    public static void main(String[] args) {
    	TMClasspathLoader.loadClasspath();
    }
}
