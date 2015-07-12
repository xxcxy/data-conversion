package com.customtime.data.conversion.domain.handler;

import java.util.ArrayList;
import java.util.List;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;

public class InterceptHandler implements TerminatHandler {
	@TMProperty(defaultValue="-1")
	private String recodeIndex;
	
	@TMProperty
	private String interceptRule;
	
	//xx[1-10]yy：group1为xx，group2为1，group3为-，group4为10，group5为yy
	java.util.regex.Pattern utilPattern = java.util.regex.Pattern.compile("([^\\[\\]]*)?\\[\\s*(\\d*|end|first)\\s*(?:\\s*(-)\\s*(\\d*|end|first)\\s*)?\\s*\\]([^\\[\\]]*)?");
	java.util.regex.Matcher utilMatcher = null;
	
	java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("\\s*(?:-)?\\d+\\s*");
	java.util.regex.Matcher numMatcher = null;
	
	//(?:(?:,)?(?:\d+)(?:,)?)*
	java.util.regex.Pattern checkPattern = java.util.regex.Pattern.compile("(?:\\s*(?:,)?\\s*(\\d+)?\\s*(?:,)?\\s*)+");
	java.util.regex.Matcher checkMatcher = null;
	java.util.regex.Pattern splitPattern = java.util.regex.Pattern.compile("\\s*(?:,)?\\s*(\\d+)?\\s*(?:,)?\\s*");
	java.util.regex.Matcher splitMatcher = null;
	
	private Integer[] recodeIndexArr;
	
	public Recode process(Recode rd) {
		boolean needItr = false;
		recodeIndex = recodeIndex.trim();
		numMatcher = numPattern.matcher(recodeIndex);
		if(numMatcher.matches()){
			recodeIndexArr = new Integer[1];
			recodeIndexArr[0] = Integer.valueOf(recodeIndex);
			if(recodeIndexArr[0].intValue()<0){
				needItr = true;
			}
		}else{
			checkMatcher = checkPattern.matcher(recodeIndex);
			if(checkMatcher.matches()){
				splitMatcher = splitPattern.matcher(recodeIndex);
				List<Integer> list = new ArrayList<Integer>();
				while(splitMatcher.find()){
					String str = splitMatcher.group(1);
					if(str==null||"".equals(str = str.trim())){
						continue;
					}else{
						list.add(Integer.valueOf(str));
					}
				}
				if(list.size()==0){
					needItr = true;
				}else{
					recodeIndexArr = list.toArray(new Integer[list.size()]);
				}
			}else{
				needItr = true;
			}
			
		}
		
		
		if(needItr){
			for(int i=0,len=rd.getBlockNum();i<len;i++){
				rd.putBlock(dealFiled(rd.getBlock(i)), i);
			}
		}else{
			for(int j=0;j<recodeIndexArr.length;j++){
				int index = recodeIndexArr[j].intValue();
				if(index<rd.getBlockNum()){
					rd.putBlock(dealFiled(rd.getBlock(index)), index);
				}
			}
			
		}
		
		return rd;
	}
	
	private String dealFiled(String str){
		if(str==null||interceptRule==null||"".equals(interceptRule)){
			return str;
		}
		int strLength = str.length();
		StringBuffer sb = new StringBuffer();
		//xx[1-10]yy：group1为xx，group2为1，group3为-，group4为10，group5为yy
		java.util.regex.Matcher utilMatcher = utilPattern.matcher(interceptRule);
		
		boolean flag = false;
		while(utilMatcher.find()){
			
			flag = true;
			String pre = utilMatcher.group(1);
			if(pre==null){
				pre = "";
			}
			String fin = utilMatcher.group(5);
			if(fin == null){
				fin = "";
			}
			
			String start = utilMatcher.group(2);
			String end = utilMatcher.group(4);
			String to = utilMatcher.group(3);
			
			//没有start和end则不处理
			if(to==null||"".equals(to = to.trim())||start==null||"".equals(start = start.trim())||end==null||"".equals(end = end.trim())||strLength==0){
				sb.append(pre);
				sb.append(fin);
			}else{
				boolean isReverse = false;
				if("0".equals(start)){
					start = "first";
				}

				
				boolean isStartNum = false;
				numMatcher = numPattern.matcher(start);
				if((isStartNum = numMatcher.matches())&&Integer.valueOf(start).intValue()>=(strLength-1)){
					start = "end";
					isStartNum = false;
				}
				boolean isEndNum = false;
				numMatcher = numPattern.matcher(end);
				if((isEndNum = numMatcher.matches())&&Integer.valueOf(end).intValue()>=(strLength-1)){
					end = "end";
					isEndNum = false;
				}
				
				if("end".equals(start)||(isStartNum&&isEndNum&&Integer.valueOf(start).intValue()>Integer.valueOf(end).intValue())){
					isReverse = true;
					String t = start;
					start = end;
					end = t;
					boolean tt = isStartNum;
					isStartNum = isEndNum;
					isEndNum = tt;
				}
				
				int subStartIndex = 0;
				if(isStartNum){//经过上述逻辑处理后，start只可能为first、end或者数字的字符串
					subStartIndex = Integer.valueOf(start).intValue();
				}else if("end".equals(start)){
					subStartIndex = strLength-1;
				}
				
				int subEndIndex = strLength-1;
				if(isEndNum){//经过上述逻辑处理后，end只可能为first、end或者数字的字符串
					subEndIndex = Integer.valueOf(end).intValue();
				}else if("first".equals(end)){
					subEndIndex = 0;
				}
				
				String newStr = str.substring(subStartIndex, subEndIndex+1);
				if(isReverse){
					StringBuilder sbd = new StringBuilder(newStr);
					newStr = sbd.reverse().toString();
				}
				sb.append(pre);
				sb.append(newStr);
				sb.append(fin);
			}
		}
		
		if(!flag){
			return str;
		}
		return sb.toString();
	}
	
	public static void main(String[] args){
		InterceptHandler ih = new InterceptHandler();
		ih.recodeIndex = " , , 2, ,55 , , 4 , ";
		ih.process(null);
		/*
		String str = "2012-11-27 00:09:29.000000025";//字符串长度为29
		InterceptHandler ih = new InterceptHandler();
		String result;
		
		System.out.println("record为空开始=======");
		ih.interceptRule = "[14-15]";
		result = ih.dealFiled(null);
		System.out.println("00"+result);
		System.out.println("record为空开始=======");
		System.out.println("record为空字符串开始=======");
		ih.interceptRule = "[14-15]";
		result = ih.dealFiled("");
		System.out.println("00"+result);
		ih.interceptRule = "#[14-15]x";
		result = ih.dealFiled("");
		System.out.println("00"+result);
		System.out.println("record为字符串空开始=======");
		System.out.println("interceptRule为空开始=======");
		ih.interceptRule = null;
		result = ih.dealFiled("abc");
		System.out.println("00"+result);
		System.out.println("interceptRule为空开始=======");
		System.out.println("interceptRule为空字符串开始=======");
		ih.interceptRule = "";
		result = ih.dealFiled("abc");
		System.out.println("00"+result);
		System.out.println("interceptRule为空开始=======");
		
		ih.interceptRule = "[14-15]";
		result = ih.dealFiled(str);
		System.out.println("01"+result);
		ih.interceptRule = "[11-12][14-15]";
		result = ih.dealFiled(str);
		System.out.println("02"+result);
		ih.interceptRule = "[14-15][11-12]";
		result = ih.dealFiled(str);
		System.out.println("03"+result);
		ih.interceptRule = "##[11-12][14-15]";
		result = ih.dealFiled(str);
		System.out.println("04"+result);
		ih.interceptRule = "##[11-12]::[14-15]";
		result = ih.dealFiled(str);
		System.out.println("05"+result);
		ih.interceptRule = "##[11-12]::[14-15]...";
		result = ih.dealFiled(str);
		System.out.println("06"+result);
		ih.interceptRule = "##[11-12]::[15-14]...";
		result = ih.dealFiled(str);
		System.out.println("07"+result);
		ih.interceptRule = "##[11-12]::[15-end]...";
		result = ih.dealFiled(str);
		System.out.println("08"+result);
		ih.interceptRule = "##[11-12]::[15-28]...";
		result = ih.dealFiled(str);
		System.out.println("09"+result);
		ih.interceptRule = "##[11-12]::[28-15]...";
		result = ih.dealFiled(str);
		System.out.println("10"+result);
		ih.interceptRule = "##[11-12]::[end-15]...";
		result = ih.dealFiled(str);
		System.out.println("11"+result);
		ih.interceptRule = "##[11-12]::[end-first]...";
		result = ih.dealFiled(str);
		System.out.println("12"+result);
		System.out.println("非法的[]开始=================");
		ih.interceptRule = "##[11-12]::[end-]...";
		result = ih.dealFiled(str);
		System.out.println("13"+result);
		ih.interceptRule = "##[11-12]::[first-]...";
		result = ih.dealFiled(str);
		System.out.println("14"+result);
		ih.interceptRule = "##[11-12]::[-]...";
		result = ih.dealFiled(str);
		System.out.println("15"+result);
		ih.interceptRule = "##[11-12]::[-end]...";
		result = ih.dealFiled(str);
		System.out.println("16"+result);
		ih.interceptRule = "##[11-12]::[-first]...";
		result = ih.dealFiled(str);
		System.out.println("17"+result);
		System.out.println("非法的[]结束=================");
		
		ih.interceptRule = "##[11-12]::[first-15]...";
		result = ih.dealFiled(str);
		System.out.println("18"+result);
		ih.interceptRule = "##[ 11 - 12 ]::[ first - end ]...";
		result = ih.dealFiled(str);
		System.out.println("19"+result);
		ih.interceptRule = "##[ 11 - 12 ]::[ end - end ]...";
		result = ih.dealFiled(str);
		System.out.println("20"+result);
		ih.interceptRule = "##[ 11 - 12 ]::[ first - first ]...";
		result = ih.dealFiled(str);
		System.out.println("21"+result);
		
		ih.interceptRule = "##[ 29 - 29 ]::[ first - first ]...";
		result = ih.dealFiled(str);
		System.out.println("22"+result);
		
		ih.interceptRule = "##[ 29 - end ]::[ first - first ]...";
		result = ih.dealFiled(str);
		System.out.println("23"+result);
		
		ih.interceptRule = "##[ 28 - 29 ]::[ first - first ]...";
		result = ih.dealFiled(str);
		System.out.println("24"+result);
		
		ih.interceptRule = "##[ 27 - end ]::[ first - first ]...";
		result = ih.dealFiled(str);
		System.out.println("25"+result);
		
		ih.interceptRule = "##[ end - 29 ]::[ first - first ]...";
		result = ih.dealFiled(str);
		System.out.println("26"+result);
		*/
	}

}
