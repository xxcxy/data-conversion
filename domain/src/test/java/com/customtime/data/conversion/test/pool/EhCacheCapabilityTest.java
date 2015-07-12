package com.customtime.data.conversion.test.pool;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.keeper.DefaultRecode;
import com.customtime.data.conversion.test.bean.KeyObject;

public class EhCacheCapabilityTest {
	private final static Log logger = LogFactory.getLog(EhCacheCapabilityTest.class);
	
	public void testCapability(){
		CacheManager cacheManager = new CacheManager();
		Cache cache = cacheManager.getCache("CapabilityTest");
		Long startTime = System.currentTimeMillis();
		Map<String,TreeSet<KeyObject>> keyMap = new HashMap<String,TreeSet<KeyObject>>(1000000);
		for(int i=0;i<100;i++){
			for(int j=0;j<10000;j++){
				KeyObject ko = new KeyObject("111.111.111.111,8080,8080,10.1.1."+j,i*10000l+j,Long.valueOf(i*j));
				put(ko,keyMap);
				DefaultRecode rd = new DefaultRecode();
				rd.context("'2012-11-27 00:00:14.000000029','2012-11-27 00:00:14.000000029','172.16.0.199','1036','221.179.180.79','80'");
				cache.put(new Element("111.111.111.111,8080,8080,10.1.1."+j,"'2012-11-27 00:00:14.000000029','2012-11-27 00:00:14.000000029','172.16.0.199','1036','221.179.180.79','80'"));
			}
		}
		logger.info(System.currentTimeMillis()-startTime);	
		Long getStartTime = System.currentTimeMillis();
		int count=0;
		for(int i=0;i<100;i++){
			for(int j=0;j<10000;j++){
				String key = "10.1.1."+j;
				long m1 = 5*10000l;
				KeyObject kob = getKeyObject(key,m1,keyMap);
				if(kob!=null){
					if(cache.get(kob)!=null)
						count++;
				}
			}
		}
		logger.info(count);
		logger.info(System.currentTimeMillis()-getStartTime);
	}
	
	private void put(KeyObject ko,Map<String,TreeSet<KeyObject>> map){
		String key = ko.getIp();
		if(!map.containsKey(key)){
			TreeSet<KeyObject> ts = new TreeSet<KeyObject>();
			map.put(key, ts);
		}
		map.get(key).add(ko);
	}
	private KeyObject getKeyObject(String ip,long m1,Map<String,TreeSet<KeyObject>> map){
		return map.get(ip).higher(new KeyObject(ip,m1,0l));
	}
}
