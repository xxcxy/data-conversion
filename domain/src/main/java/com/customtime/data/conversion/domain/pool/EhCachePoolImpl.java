package com.customtime.data.conversion.domain.pool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.search.Direction;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bsh.EvalError;
import bsh.Interpreter;

import com.customtime.data.conversion.plugin.pool.AbstractPool;

public class EhCachePoolImpl extends AbstractPool {

	public final static CacheManager cacheManager = new CacheManager();
	private final static Log logger = LogFactory.getLog(EhCachePoolImpl.class);
	private final static Map<String, Interpreter> CacheInterpreter = new HashMap<String, Interpreter>();

	public <T, K> Map<T, K> getRecodes(String cache, List<T> key) {
		Cache cc = cacheManager.getCache(cache);
		cc.getAll(key);
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getRecode(String cache, Object key, Class<T> classType) {
		T t = null;
		try {
			t = (T) getRecode(cache, key);
		} catch (Exception e) {
			logger.error(" Error cast from Object to T");
			e.printStackTrace();
		}
		return t;
	}

	public Object getRecode(String cache, Object key) {
		return cacheManager.getCache(cache).get(key).getObjectValue();
	}

	public <K, V> void putRecode(String cache, Map<K, V> map) {
		Cache cc = cacheManager.getCache(cache);
		for (Map.Entry<K, V> et : map.entrySet())
			cc.put(new Element(et.getKey(), et.getValue()));
	}

	@Override
	public void putRecode(String cache, Object key, Object value) {
		Cache cc = cacheManager.getCache(cache);
		cc.put(new Element(key, value));
	}

	@Override
	public <M> M getRecode(String cache, String queryString, Class<M> classType,String... strings ) {
		Map<Object,M> map = getRecodeCK(cache,queryString,classType,strings);
		if(map!=null){
			for(Entry<Object,M> entry:map.entrySet()){
				return entry.getValue();
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <M> Map<Object,M> getRecodeCK(String cache, String queryString, Class<M> classType,String... strings ) {
		Cache cc = cacheManager.getCache(cache);
		Interpreter it = CacheInterpreter.get(cache);
		try {
			if (it == null) {
				it = new Interpreter();
				it.setClassLoader(EhCachePoolImpl.class.getClassLoader());
				Map<String, SearchAttribute> attributes = cc.getCacheConfiguration().getSearchAttributes();
				for (Map.Entry<String, SearchAttribute> entry : attributes.entrySet()) {
					it.set(entry.getKey(),cc.getSearchAttribute(entry.getKey()));
				}
				Results results = null;
				it.set("results", results);
				it.set("Direction", Direction.ASCENDING);
				CacheInterpreter.put(cache, it);
			}
			it.set("query", cc.createQuery().includeKeys().includeValues());
			StringBuffer fullQueryString = new StringBuffer("results = query.addCriteria(").append(queryString).append(")");
			for(String str:strings){
				if(str!=null && !"".equals(str))
					fullQueryString.append(".").append(str);
			}
			fullQueryString.append(".execute()");
			it.eval(fullQueryString.toString());
			Results results= (Results)it.get("results");
			if(results!=null && results.all()!=null && results.all().size()>0){
				Result result = results.all().get(0);
				if(result !=null){
					Map<Object,M> rm = new HashMap<Object,M>();
					rm.put(result.getKey(),(M)result.getValue());
					return rm;
				}
			}
		} catch (CacheException e) {
			e.printStackTrace();
		} catch (EvalError e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getCacheSize(String cache) {
		return cacheManager.getCache(cache).getSize();
	}
}
