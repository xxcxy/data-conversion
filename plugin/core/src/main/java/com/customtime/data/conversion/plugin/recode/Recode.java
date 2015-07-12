package com.customtime.data.conversion.plugin.recode;

public interface Recode {

	public long length();
	public void context(String context);
	public String getContext();
	public void putBlock(String str);
	public void putBlock(String str,int index);
	public String getBlock(int index);
	public String popBlock();
	public void context(String scontext,String split);
	public String getContext(String split);
	public int getBlockNum();
	public Recode newRecode();
}
