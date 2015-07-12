package com.customtime.data.conversion.plugin.writer;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.context.PluginContext;
import com.customtime.data.conversion.plugin.pool.AbstractPool;
import com.customtime.data.conversion.plugin.pool.KeyObject;
import com.customtime.data.conversion.plugin.pool.TerminatorCache;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;

public class PoolWriter extends AbstractWriter {
	private final static Log logger = LogFactory.getLog(PoolWriter.class);
	@TMProperty
	private String keyString;
	@TMProperty
	private String parseM1;
	@TMProperty
	private String parseM2;
	@TMProperty
	private String parseM3;
	@TMProperty
	private String parseM4;
	@TMProperty
	private String cacheName;
	private int[] keyStrCols;
	private int pm1col;
	private SimpleDateFormat sf1;
	private int pm2col;
	private SimpleDateFormat sf2;
	private int pm3col;
	private SimpleDateFormat sf3;
	private int pm4col;
	private SimpleDateFormat sf4;
	private AbstractPool pool;
	public void init(){
		String[] keyStrColsT = keyString.split(",");
		keyStrCols = new int[keyStrColsT.length];
		for(int i=0,len=keyStrColsT.length;i<len;i++){
			keyStrCols[i]= Integer.parseInt(keyStrColsT[i]);
		}
		if("".equals(parseM1)){
			pm1col= -1;
		}else{
			String[] m1s=parseM1.split(";");
			pm1col = Integer.parseInt(m1s[0]);
			if(m1s.length>1)
				sf1 = new SimpleDateFormat(m1s[1]);
		}
		if("".equals(parseM2)){
			pm2col= -1;
		}else{
			String[] m1s=parseM2.split(";");
			pm2col = Integer.parseInt(m1s[0]);
			if(m1s.length>1)
				sf2 = new SimpleDateFormat(m1s[1]);
		}
		if("".equals(parseM3)){
			pm3col= -1;
		}else{
			String[] m3s=parseM3.split(";");
			pm3col = Integer.parseInt(m3s[0]);
			if(m3s.length>1)
				sf3 = new SimpleDateFormat(m3s[1]);
		}
		if("".equals(parseM2)){
			pm2col= -1;
		}else{
			String[] m4s=parseM4.split(";");
			pm4col = Integer.parseInt(m4s[0]);
			if(m4s.length>1)
				sf4 = new SimpleDateFormat(m4s[1]);
		}
		pool = PluginContext.getPool();
	}
	
	public void writing(RecodesKeeper rk) {
		logger.info("start writing");
		Recode recode;
		if(cacheName==null || "".equals(cacheName)){
			return;
		}
		while ((recode = rk.arising(this)) != null) {
			StringBuffer sb = new StringBuffer(recode.getBlock(keyStrCols[0]));
			KeyObject ko= new KeyObject();
			for(int i=1,len=keyStrCols.length;i<len;i++){
				sb.append(",").append(recode.getBlock(keyStrCols[i]));
			}
			ko.setEquString(sb.toString());
			setKo(ko,recode);
			pool.putRecode(cacheName, ko.toString(),recode.getContext());
			TerminatorCache.putKeyObject(cacheName, sb.toString(), ko);
		}
		logger.info(pool.getCacheSize(cacheName));
		logger.info("end writing to cache!");
	}
	
	private void setKo(KeyObject ko,Recode recode){
		if(pm1col!=-1){
			String cv = recode.getBlock(pm1col);
			if(sf1!=null)
				try {
					ko.setM1(sf1.parse(cv).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			else
				ko.setM1(Long.parseLong(cv));
			
		}
		if(pm2col!=-1){
			String cv = recode.getBlock(pm2col);
			if(sf2!=null)
				try {
					ko.setM2(sf2.parse(cv).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			else
				ko.setM2(Long.parseLong(cv));
			
		}
		if(pm3col!=-1){
			String cv = recode.getBlock(pm3col);
			if(sf3!=null)
				try {
					ko.setM3(sf3.parse(cv).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			else
				ko.setM3(Long.parseLong(cv));
			
		}
		if(pm4col!=-1){
			String cv = recode.getBlock(pm4col);
			if(sf4!=null)
				try {
					ko.setM4(sf4.parse(cv).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			else
				ko.setM4(Long.parseLong(cv));
			
		}
	}
}
