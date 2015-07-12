package com.customtime.data.conversion.domain.handler;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;

public class Operat2FiledHandler implements TerminatHandler {
	@TMProperty(defaultValue="-1")
	private int col1;
	@TMProperty(defaultValue="-1")
	private int col2;
	@TMProperty(defaultValue="-1")
	private int operatType;

	public Recode process(Recode rd) {
		if(operatType==0){
			rd.putBlock(Long.parseLong(rd.getBlock(col1))+Long.parseLong(rd.getBlock(col2))+"");
		}else if(operatType==1){
			rd.putBlock(Long.parseLong(rd.getBlock(col1))-Long.parseLong(rd.getBlock(col2))+"");
		}else if(operatType==2){
			rd.putBlock(Long.parseLong(rd.getBlock(col1))*Long.parseLong(rd.getBlock(col2))+"");
		}else if(operatType==3){
			rd.putBlock(Long.parseLong(rd.getBlock(col1))/Long.parseLong(rd.getBlock(col2))+"");
		}
		return rd;
	}
}
