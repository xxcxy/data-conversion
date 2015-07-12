package com.customtime.data.conversion.domain.keeper;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.customtime.data.conversion.plugin.recode.Recode;

public class DefaultRecode implements Recode,Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Recode newRecode() {
		return new DefaultRecode();
	}

//	@TMProperty(defaultValue="32")
	private int arraySize=64;
	private String[] context;
	private long byteSize;
	private int currentNum;

	public DefaultRecode(){
		context = new String[arraySize];
		currentNum = 0;
		byteSize = 0;
	}
	
	public void putBlock(String str){
		context[currentNum]=str;
		currentNum++;
//		byteSize +=str.getBytes().length;
	}
	
	public void putBlock(String str,int index){
		if(index>=currentNum){
			context[index]=str;
			currentNum=index+1;
//			byteSize +=str.getBytes().length;
		}else if(index>=0){
//			if(context[index]!=null)
//				byteSize -=context[index].getBytes().length;
			context[index]=str;
//			byteSize +=str.getBytes().length;
		}
	}
	
	public String getBlock(int index){
		if(index<currentNum)
			return context[index];
		else
			return null;
	}
	
	public String popBlock(){
		if(currentNum>0){
			currentNum--;
			String str = context[currentNum];
//			if(str!=null)
//				byteSize -=str.getBytes().length;
			return str;
		}else{
			return null;
		}
	}
	
	public long length() {
		return byteSize;
	}

	public void context(String context){
		context(context,",");
	}
	public void context(String scontext,String split) {
		String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(scontext,split);
		for(String s:ss){
			context[currentNum]=s;
			currentNum++;
//			byteSize +=s.getBytes().length;
		}
	}

	public String getContext() {
		return getContext(",");
	}
	public String getContext(String split){
		return StringUtils.join(context, split,0,currentNum);
	}

	public int getBlockNum() {
		return currentNum;
	}
	

}
