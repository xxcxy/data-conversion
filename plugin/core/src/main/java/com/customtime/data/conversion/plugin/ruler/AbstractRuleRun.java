package com.customtime.data.conversion.plugin.ruler;

public abstract class AbstractRuleRun {
	private int hashCode;
	public void init(){
		
	}
	public void destroy(){
		
	}
	
	public boolean equals(Object obj){
		if(obj instanceof AbstractRuleRun){
			if(((AbstractRuleRun)obj).hashCode!=0 && hashCode != 0)
				return hashCode == ((AbstractRuleRun)obj).hashCode;
		}
		return super.equals(obj);
	}
	public Object clone(){
		Object obj = null;
		try {
			if(hashCode==0)
				hashCode = this.hashCode();
			obj = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public int hashCode(){
		if(hashCode == 0)
			hashCode = super.hashCode();
		return hashCode;
	}
}
