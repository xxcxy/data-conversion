package com.customtime.data.conversion.domain.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class PluginAppClassLoader extends URLClassLoader {

	private final ClassLoader _parent;
	private final Set<String> _extensions = new HashSet<String>();
	private String _name = String.valueOf(hashCode());

	public PluginAppClassLoader(){
		this(null);
	}

	public PluginAppClassLoader(ClassLoader parent){
		super(new URL[] {}, parent != null ? parent : (Thread.currentThread().getContextClassLoader() != null ? Thread.currentThread().getContextClassLoader() : (PluginAppClassLoader.class.getClassLoader() != null ? PluginAppClassLoader.class.getClassLoader() : ClassLoader.getSystemClassLoader())));
		_parent = getParent();
		if (_parent == null)
			throw new IllegalArgumentException("no parent classloader!");

		_extensions.add(".jar");
		_extensions.add(".zip");

		// TODO remove this system property
		String extensions = System.getProperty(PluginAppClassLoader.class
				.getName() + ".extensions");
		if (extensions != null) {
			StringTokenizer tokenizer = new StringTokenizer(extensions, ",;");
			while (tokenizer.hasMoreTokens())
				_extensions.add(tokenizer.nextToken().trim());
		}

//		if (getExtraClasspath() != null)
//			addClassPath(context.getExtraClasspath());
	}

	public String getName(){
        return _name;
    }
	
	public void setName(String name){
		_name = name;
	}
	
	private boolean isSystemClass(String name){
		return false;
	}
	
	private boolean isServerClass(String name){
		return false;
	}
	
	private boolean isParentLoaderPriority(){
		return false;
	}
	
	@Override
    public Class<?> loadClass(String name) throws ClassNotFoundException{
        return loadClass(name, false);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
        Class<?> c= findLoadedClass(name);
        ClassNotFoundException ex= null;
        boolean tried_parent= false;
        
        boolean system_class=isSystemClass(name);
        boolean server_class=isServerClass(name);
        
        if (system_class && server_class){
            return null;
        }
        
        if (c == null && _parent!=null && (isParentLoaderPriority() || system_class) && !server_class){
            tried_parent= true;
            try{
                c= _parent.loadClass(name);
//                if (Log.isDebugEnabled())
//                    Log.debug("loaded " + c);
            }catch (ClassNotFoundException e){
                ex= e;
            }
        }

        if (c == null){
            try{
                c= this.findClass(name);
            }
            catch (ClassNotFoundException e){
                ex= e;
            }
        }

        if (c == null && _parent!=null && !tried_parent && !server_class )
            c= _parent.loadClass(name);

        if (c == null)
            throw ex;

        if (resolve)
            resolveClass(c);

//        if (Log.isDebugEnabled())
//            Log.debug("loaded " + c+ " from "+c.getClassLoader());
        
        return c;
    }

    public String toString(){
        return "PluginAppClassLoader=" + _name+"@"+Long.toHexString(hashCode());
    }
}
