package com.customtime.data.conversion.test.bean;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheEvent implements CacheEventListener {
	private static final Log logger = LogFactory.getLog(CacheEvent.class);

	public void dispose() {
		logger.info("dispose!");
	}

	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
		logger.info("notifyElementEvicted!");
		logger.info("fangwen cishu:"+arg1.getHitCount() + " zuihou fangwen shijian:"+arg1.getLastAccessTime());
		
	}

	public void notifyElementExpired(Ehcache arg0, Element arg1) {
		logger.info("notifyElementExpired!");
		logger.info(arg1);
		logger.info("fangwen cishu:"+arg1.getHitCount() + " zuihou fangwen shijian:"+arg1.getLastAccessTime());
		
	}

	public void notifyElementPut(Ehcache arg0, Element arg1)
			throws CacheException {
		logger.info("notifyElementPut!");
		logger.info("fangwen cishu:"+arg1.getHitCount() + " zuihou fangwen shijian:"+arg1.getLastAccessTime());
		
	}

	public void notifyElementRemoved(Ehcache arg0, Element arg1)
			throws CacheException {
		logger.info("notifyElementRemoved!");
		logger.info("fangwen cishu:"+arg1.getHitCount() + " zuihou fangwen shijian:"+arg1.getLastAccessTime());
		
	}

	public void notifyElementUpdated(Ehcache arg0, Element arg1)
			throws CacheException {
		logger.info("notifyElementUpdated!");
		logger.info("fangwen cishu:"+arg1.getHitCount() + " zuihou fangwen shijian:"+arg1.getLastAccessTime());
		
	}

	public void notifyRemoveAll(Ehcache arg0) {
		logger.info("notifyRemoveAll!");
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
