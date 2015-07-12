package com.customtime.data.conversion.domain.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;

public class FormatHandler implements TerminatHandler {
	@TMProperty
	private int col;
	@TMProperty
	private int operatType;// 0 express + , 1 express * , 2 express * , 3 express / , 4 express long to dateString , 5 express dateString to long
	@TMProperty
	private String param;
	private SimpleDateFormat sf;

	public Recode process(Recode rd) {
		String str = rd.getBlock(col);
		try {
			if (operatType == 0) {
				long l1 = Long.parseLong(str) + Long.parseLong(param);
				str = l1 + "";
			}else if(operatType==1){
				long l1 = Long.parseLong(str) - Long.parseLong(param);
				str = l1 + "";
			}else if(operatType==2){
				long l1 = Long.parseLong(str)* Long.parseLong(param);
				str = l1 + "";
			}else if(operatType==3){
				long l1 = Long.parseLong(str)/Long.parseLong(param);
				str = l1 + "";
			}else if(operatType==4){
				if(sf==null)
					sf = new SimpleDateFormat(param);
				long l1 = Long.parseLong(str);
				str = sf.format(new Date(l1));
			}else if(operatType==5){
				if(sf==null)
					sf = new SimpleDateFormat(param);
				str = sf.parse(str).getTime()+"";
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rd.putBlock(str,col);
		return rd;
	}

}
