package com.customtime.data.conversion.domain.temporary;

import com.customtime.data.conversion.plugin.recode.Recode;

public interface Temporary {

	public boolean isPushClosed();
	public String info();
	public boolean init(String id, int lineLimit, int byteLimit,int destructLimit);
	public boolean push(Recode recode);
	public boolean push(Recode[] recodes, int size);
	public boolean fakePush(int lineLength);
	public Recode pull();
	public int pull(Recode[] recodes) ;
	public int size();
	public boolean empty();
	public int getLineLimit() ;
	public void setPushClosed();
	public Temporary setMakeObject(Recode recode);
	
}
