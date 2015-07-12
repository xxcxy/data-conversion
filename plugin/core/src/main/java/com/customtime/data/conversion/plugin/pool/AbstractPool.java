package com.customtime.data.conversion.plugin.pool;

import java.util.List;
import java.util.Map;

public abstract class AbstractPool {
	public abstract <T,K> Map<T,K> getRecodes(String cache,List<T> key);
	public abstract <T> T getRecode(String cache,Object key,Class<T> classType);
	public abstract Object getRecode(String cache,Object key);
	public abstract <K,V> void putRecode(String cache,Map<K,V> map);
	public abstract void putRecode(String cache,Object key,Object valie);
	public abstract <T> T getRecode(String cache,String queryString,Class<T> classType,String... strings);
	public abstract <M> Map<Object,M> getRecodeCK(String cache, String queryString, Class<M> classType,String... strings );
	public abstract long getCacheSize(String cache);
}
