package com.customtime.data.conversion.domain.handler;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;

public class JoinBlockHandler implements TerminatHandler{

	@TMProperty
	private String join_rule;
	@TMProperty(defaultValue=",")
	private String splitStr;
	@TMProperty
	private String replaceNull;
	public Recode process(Recode rd) {
		Recode recode = rd.newRecode();
		String[] rols = join_rule.split(",");
		for(String str:rols){
			if("".equals(str)||str==null){
				recode.putBlock(replaceNull);
			}else{
				String[] cols = str.split(";");
				StringBuffer sb = new StringBuffer(rd.getBlock(Integer.parseInt(cols[0])));
				for(int i=1,len=cols.length;i<len;i++)
					sb.append(splitStr).append(cols[i]);
				recode.putBlock(sb.toString());
			}
		}
		return recode;
	}
	
}
