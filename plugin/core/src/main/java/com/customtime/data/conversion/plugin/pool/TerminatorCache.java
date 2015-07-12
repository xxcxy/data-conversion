package com.customtime.data.conversion.plugin.pool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class TerminatorCache {
	private static final Map<String,Map<String,TreeSet<KeyObject>>> cache = new HashMap<String,Map<String,TreeSet<KeyObject>>>();
	
	public static KeyObject getKeyObject(String cacheName,String key,long m1,long m2,int type){
		Map<String,TreeSet<KeyObject>> cacheE = cache.get(cacheName);
		if(cacheE!=null){
			TreeSet<KeyObject> ts = cacheE.get(key);
			if(ts !=null){
				if(type<0)
					return ts.lower(new KeyObject(key,m1,m2));
				else
					return ts.higher(new KeyObject(key,m1,m2));
			}
			
		}
		return null;
	}
	
	public static KeyObject getKeyObject(String cacheName,String key,long m1,long m2,int type,long m3){
		Map<String,TreeSet<KeyObject>> cacheE = cache.get(cacheName);
		if(cacheE!=null){
			TreeSet<KeyObject> ts = cacheE.get(key);
			Set<KeyObject> rs = null;
			if(ts !=null){
				if(type<0)
					rs =ts.headSet(new KeyObject(key,m1,m2), false);
				else
					rs = ts.tailSet(new KeyObject(key,m1,m2),false);
			}
			if(rs!=null){
				for(KeyObject ko:rs){
					if(ko.getM3()>=m3&&ko.getM4()<=m3)
						return ko;
				}
			}
		}
		return null;
	}
	
	public static synchronized boolean putKeyObject(String cacheName,String key ,KeyObject ko){
		Map<String,TreeSet<KeyObject>> cacheE = cache.get(cacheName);
		if(cacheE == null){
			cacheE = createMap(cacheName);
		}
		TreeSet<KeyObject> ts = cacheE.get(key);
		if(ts == null){
			ts = createSet(cacheE,key);
		}
		return ts.add(ko);
	}
	private static synchronized Map<String,TreeSet<KeyObject>> createMap(String cacheName){
		Map<String,TreeSet<KeyObject>> cacheE = cache.get(cacheName);
		if(cacheE==null){
			cacheE = new HashMap<String,TreeSet<KeyObject>>(10000);
			cache.put(cacheName, cacheE);
		}
		return cacheE;
	}
	private static synchronized TreeSet<KeyObject> createSet(Map<String,TreeSet<KeyObject>> map,String key){
		TreeSet<KeyObject> ts = map.get(key);
		if(ts==null){
			ts = new TreeSet<KeyObject>();
			map.put(key, ts);
		}
		return ts;
	}
	
	public static boolean remove(String cacheName,String key,KeyObject ko){
		Map<String,TreeSet<KeyObject>> cacheE = cache.get(cacheName);
		if(cacheE != null){
			TreeSet<KeyObject> ts = cacheE.get(key);
			if(ts!=null){
				boolean rt = ts.remove(ko);
				if(ts.isEmpty()){
					cacheE.remove(key);
				}
				return rt;
			}
		}
		return false;
	}
	public static boolean remove(String cacheName,String key,String ko){
		Map<String,TreeSet<KeyObject>> cacheE = cache.get(cacheName);
		if(cacheE != null){
			TreeSet<KeyObject> ts = cacheE.get(key);
			if(ts!=null){
				boolean rt = false;
				KeyObject rko = null;
				for(KeyObject kot:ts){
					if(kot.equals(ko)){
						rko = kot;
						rt = true;
						break;
					}
				}
				if(rt){
					ts.remove(rko);
				}
				if(ts.isEmpty()){
					cacheE.remove(key);
				}
				return rt;
			}
		}
		return false;
	}
}
