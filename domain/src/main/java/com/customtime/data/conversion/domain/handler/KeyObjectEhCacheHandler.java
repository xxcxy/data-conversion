package com.customtime.data.conversion.domain.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Direction;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.expression.Criteria;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.annotation.InjectMonitor;
import com.customtime.data.conversion.domain.pool.EhCachePoolImpl;
import com.customtime.data.conversion.plugin.annotaion.CallMonitor;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.pool.KeyObject;
import com.customtime.data.conversion.plugin.pool.TerminatorCache;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.util.MonitorUtil;

public class KeyObjectEhCacheHandler implements TerminatHandler {
	private static final Log logger = LogFactory
			.getLog(KeyObjectEhCacheHandler.class);
	@TMProperty
	private String cacheName;
	@TMProperty
	private String keyStringCol;
	@TMProperty
	private int m1ConditionType;// 1(le) 2(lt) 3(ge) 4(gt)
	@TMProperty(defaultValue = "-1")
	private int m1Col;
	@TMProperty
	private String m1Fmt;
	@TMProperty
	private int m2ConditionType;// 1(le) 2(lt) 3(ge) 4(gt)
	@TMProperty(defaultValue = "-1")
	private int m2Col;
	@TMProperty
	private String m2Fmt;
	@TMProperty(defaultValue = "-1")
	private int m3Col;
	@TMProperty
	private String m3Fmt;
	@TMProperty
	private String rdCol;
	@TMProperty
	private int orderBy;// 1(m1 asc) 2(m1 dsc) 3(m2 asc) 4(m2 dsc)
	@TMProperty
	private String errFile;
	@TMProperty(defaultValue = ",")
	private String errSplit;
	@InjectMonitor
	private PluginMonitor pm;
	@TMProperty
	private String successLineType;
	@TMProperty
	private String failureLineType;
	private SimpleDateFormat sf1;
	private SimpleDateFormat sf2;
	private SimpleDateFormat sf3;

	public Recode process(Recode rd) {
		if (rd.getBlockNum() < 1)
			return rd;
		return newProcess(rd);
	}

	@CallMonitor
	public void monitor(MonitorUtil mu) {
		PrintWriter pw = null;
		try {
			if ("".equals(errFile))
				return;
			List<String> failines = "".equals(failureLineType) ? mu
					.getFailines() : mu.getFailines(failureLineType);
			if(failines==null||failines.size()==0){
				return;
			}
			// pw = new PrintWriter(new FileWriter(errFile,true));
			File ef = new File(errFile);
			if(!ef.exists()){
				File efdir = ef.getParentFile();
				if(!efdir.exists()){
					efdir.mkdirs();
				}
				ef.createNewFile();
			}
				
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					errFile, true), "UTF-8"));
			//List<String> failines = "".equals(failureLineType) ? mu
			//		.getFailines() : mu.getFailines(failureLineType);
			for (String line : failines) {
				pw.println(line);
			}
		} catch (FileNotFoundException e) {
			logger.error("the errfile " + errFile + " not find!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(pw!=null)
				pw.close();
		}
	}

	private Recode newProcess(Recode rd) {
		String[] keyCol = keyStringCol.split(";");
		StringBuffer sb = new StringBuffer(rd.getBlock(Integer
				.parseInt(keyCol[0])));
		for (int i = 1, len = keyCol.length; i < len; i++) {
			sb.append(",").append(rd.getBlock(Integer.parseInt(keyCol[i])));
		}
		long m1 = 0;
		long m2 = 0;
		if (m1Col > -1) {
			String m1s = rd.getBlock(m1Col);
			if (m1Fmt != null && !"".equals(m1Fmt)) {
				if (sf1 == null)
					sf1 = new SimpleDateFormat(m1Fmt);
				try {
					m1 = sf1.parse(m1s).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				m1 = Long.parseLong(m1s);
			}
		}
		if (m2Col > -1) {
			String m1s = rd.getBlock(m2Col);
			if (m2Fmt != null && !"".equals(m2Fmt)) {
				if (sf2 == null)
					sf2 = new SimpleDateFormat(m2Fmt);
				try {
					m2 = sf2.parse(m1s).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				m2 = Long.parseLong(m1s);
			}
		}
		int type = 0;
		if (m1ConditionType == 1) {
			m1++;
			m2++;
			type = -1;
		} else if (m1ConditionType == 2) {
			type = -1;
		} else if (m1ConditionType == 3) {
			m1--;
			m2--;
			type = 1;
		} else if (m1ConditionType == 4) {
			type = 1;
		}
		KeyObject ko = null;
		if (m3Col > -1) {
			long m3 = 0;
			String m3s = rd.getBlock(m3Col);
			if (m3Fmt != null && !"".equals(m3Fmt)) {
				if (sf3 == null)
					sf3 = new SimpleDateFormat(m3Fmt);
				try {
					m3 = sf3.parse(m3s).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				m3 = Long.parseLong(m3s);
			}
			ko = TerminatorCache.getKeyObject(cacheName, sb.toString(), m1, m2,
					type, m3);
		} else
			ko = TerminatorCache.getKeyObject(cacheName, sb.toString(), m1, m2,
					type);
		if (ko != null) {
			Cache cache = EhCachePoolImpl.cacheManager.getCache(cacheName);
			Element element = cache.get(ko.toString());
			if (element != null) {
				String value =  element.getValue().toString();
				Recode qrd = rd.newRecode();
				qrd.context(value);
				for (String col : rdCol.split(",")) {
					rd.putBlock(qrd.getBlock(Integer.parseInt(col)));
				}
				if ("".equals(successLineType))
					pm.successLint();
				else
					pm.successLine(successLineType);
				return rd;
			}
		}
		if ("".equals(failureLineType))
			pm.failureLine(rd.getContext(errSplit));
		else
			pm.failureLine(failureLineType, rd.getContext(errSplit));

		return rd.newRecode();
	}

	@SuppressWarnings("unused")
	private Recode oldProcess(Recode rd) {
		String[] keyCol = keyStringCol.split(";");
		StringBuffer sb = new StringBuffer(rd.getBlock(Integer
				.parseInt(keyCol[0])));
		for (int i = 1, len = keyCol.length; i < len; i++) {
			sb.append(",").append(rd.getBlock(Integer.parseInt(keyCol[i])));
		}
		Cache cache = EhCachePoolImpl.cacheManager.getCache(cacheName);
		Attribute<Long> m1 = cache.getSearchAttribute("m1");
		Attribute<Long> m2 = cache.getSearchAttribute("m2");
		Attribute<String> equString = cache.getSearchAttribute("equString");
		Criteria ce = equString.eq(sb.toString());
		if (m1Col > -1) {
			String m1s = rd.getBlock(m1Col);
			long m1l = 0;
			if (m1Fmt != null && !"".equals(m1Fmt)) {
				if (sf1 == null)
					sf1 = new SimpleDateFormat(m1Fmt);
				try {
					m1l = sf1.parse(m1s).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				m1l = Long.parseLong(m1s);
			}
			if (m1ConditionType == 1)
				ce = ce.and(m1.le(m1l));
			else if (m1ConditionType == 2)
				ce = ce.and(m1.lt(m1l));
			else if (m1ConditionType == 3)
				ce = ce.and(m1.ge(m1l));
			else if (m1ConditionType == 4)
				ce = ce.and(m1.gt(m1l));
		}
		if (m2Col > -1) {
			String m2s = rd.getBlock(m2Col);
			long m2l = 0;
			if (m2Fmt != null && !"".equals(m2Fmt)) {
				if (sf2 == null)
					sf2 = new SimpleDateFormat(m2Fmt);
				try {
					m2l = sf1.parse(m2s).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				m2l = Long.parseLong(m2s);
			}
			if (m2ConditionType == 1)
				ce = ce.and(m2.le(m2l));
			else if (m2ConditionType == 2)
				ce = ce.and(m2.lt(m2l));
			else if (m2ConditionType == 3)
				ce = ce.and(m2.ge(m2l));
			else if (m2ConditionType == 4)
				ce = ce.and(m2.gt(m2l));
		}
		Query query = cache.createQuery().includeValues();
		query.addCriteria(ce);
		if (orderBy == 1)
			query.addOrderBy(m1, Direction.ASCENDING);
		else if (orderBy == 2)
			query.addOrderBy(m1, Direction.DESCENDING);
		else if (orderBy == 3)
			query.addOrderBy(m2, Direction.ASCENDING);
		else if (orderBy == 4)
			query.addOrderBy(m2, Direction.DESCENDING);
		Results results = query.maxResults(1).execute();
		if (results != null && results.all() != null
				&& results.all().size() > 0) {
			Recode qrd = (Recode) results.all().get(0).getValue();
			for (String col : rdCol.split(",")) {
				rd.putBlock(qrd.getBlock(Integer.parseInt(col)));
			}
			pm.successLint();
			return rd;
		} else {
			pm.failureLine(rd.getContext(errSplit));
			return rd.newRecode();
		}
	}
}
