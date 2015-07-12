package com.customtime.data.conversion.domain.handler;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;

public class ReplaceHandler implements TerminatHandler {
	@TMProperty
	private String startFilterStr;
	@TMProperty
	private String endFilterStr;
	
	public Recode process(Recode rd) {
		for(int i=0,len=rd.getBlockNum();i<len;i++){
			rd.putBlock(dealFiled(rd.getBlock(i)), i);
		}
		return rd;
	}
	
	private String dealFiled(String str){
		if(str.startsWith(startFilterStr))
			str = str.substring(startFilterStr.length());
		
		if(str.endsWith(endFilterStr))
			str = str.substring(0, str.length()-endFilterStr.length());
		return str;
	}

}
