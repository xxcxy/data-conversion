package com.customtime.data.conversion.test.pool;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.test.bean.KeyObject;

public class EhCacheTest {

//	private AbstractPool pool = new EhCachePoolImpl();
	private final static Log logger = LogFactory.getLog(EhCacheTest.class);
/**	private final static CacheManager cacheManager = new CacheManager();
		
	public static void main(String[] args){
		Cache cache = cacheManager.getCache("sampleCache1");
		for(int i=0;i<100;i++)
			cache.put(new Element(new KeyObject("10.1.1.1",i*100l,(i+1)*100l),i+""));
		Attribute<String> ip = cache.getSearchAttribute("ip");
		Attribute<Long> startTime = cache.getSearchAttribute("startTime");
		Attribute<Long> endTime = cache.getSearchAttribute("endTime");
		Results results = cache.createQuery().addCriteria(ip.eq("10.1.1.1")).addCriteria(startTime.lt(234l)).addCriteria(endTime.gt(234l)).includeKeys().end().execute();
		List<Result> lr = results.all();
		for(Result rs : lr)
			logger.info(cache.get(rs.getKey()).getObjectValue());
		cacheManager.shutdown();
	}
	
//	@Test
	public void ECPoolTest(){
		Map<String,ValueObject> map1 = new HashMap<String,ValueObject>();
		for(int i=0;i<100;i++)
			map1.put(i+"", new ValueObject("wwww"));
		pool.putRecode("sampleCache1", map1);
		logger.info(pool.getRecode("sampleCache1", "23",ValueObject.class).getJust());
		Map<KeyObject,ValueObject> map = new HashMap<KeyObject,ValueObject>();
		for(int i=0;i<100;i++)
			map.put(new KeyObject("10.1.1.1",i*100l,(i+1)*100l),new ValueObject("xxxx"));
		pool.putRecode("sampleCache1", map);
		ValueObject vo = pool.getRecode("sampleCache1", new KeyObject("10.1.1.1",1*100l,(1+1)*100l), ValueObject.class);
		logger.info(vo.getJust());
	}
*/	
	public void testSearchable(){
		CacheManager cacheManager = new CacheManager();
		Cache cache = cacheManager.getCache("sampleCache1");
		for(int i=0;i<100;i++)
			cache.put(new Element(new KeyObject("10.1.1.1",i*100l,(i+1)*100l),i+""));
		Attribute<String> ip = cache.getSearchAttribute("ip");
		Attribute<Long> startTime = cache.getSearchAttribute("startTime");
		Attribute<Long> endTime = cache.getSearchAttribute("endTime");
		Results results = cache.createQuery().addCriteria(ip.eq("10.1.1.1")).addCriteria(startTime.lt(234l)).addCriteria(endTime.gt(234l)).includeKeys().end().execute();
		List<Result> lr = results.all();
		for(Result rs : lr)
			logger.info(cache.get(rs.getKey()).getObjectValue());
		cacheManager.shutdown();
	}
	
	public void testListener(){
		try {
			test();
			Thread.sleep(200000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void test(){
		CacheManager cacheManager = new CacheManager();
		Cache cache = cacheManager.getCache("sampleCache3");
		cache.put(new Element("e1","value1"));
		cache.put(new Element("e2","value2"));
		cache.put(new Element("e3","value3"));
		try {
			Thread.sleep(2000);
			logger.info(cache.get("e1").getObjectValue());
			cache.evictExpiredElements();
			Thread.sleep(5000);
			cache.evictExpiredElements();
			logger.info(cache.get("e2").getObjectValue());
			Thread.sleep(4000);
			logger.info(cache.get("e1"));
			logger.info(cache.get("e3"));
			Thread.sleep(20000);
			cache.evictExpiredElements();
			Thread.sleep(20000);
			cache.evictExpiredElements();
			Thread.sleep(20000);
			cache.evictExpiredElements();
			Thread.sleep(20000);
			logger.info(cache.get("e1"));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
